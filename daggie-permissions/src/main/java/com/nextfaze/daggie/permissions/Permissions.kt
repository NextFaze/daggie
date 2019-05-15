@file:Suppress("unused")

package com.nextfaze.daggie.permissions

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.optional.filterPresent
import com.nextfaze.daggie.optional.toOptional
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

/** Provides a production-suitable [Permissions] binding. */
@Module
class PermissionsModule {
    @Provides @IntoSet
    internal fun initializer(realPermissions: RealPermissions): Initializer<Application> = {
        realPermissions.run().subscribe()
    }

    @Provides @IntoSet
    internal fun activityLifecycleCallbacks(realPermissions: RealPermissions): ActivityLifecycleCallbacks =
        realPermissions.activityLifecycleCallbacks

    @Provides @Singleton
    internal fun permissions(realPermissions: RealPermissions): Permissions = realPermissions
}

/** Coordinates permissions so clients can check for and request them without needing an [Activity]. */
interface Permissions {
    /** Requests the specified permissions, if necessary. */
    @CheckResult
    fun requestPermissions(vararg permissions: String): Completable = requestPermissions(permissions.toList())

    /** Requests the specified permissions, if necessary. */
    @CheckResult
    fun requestPermissions(permissions: Iterable<String>): Completable

    /** Invoked by an [Activity] during [Activity.onRequestPermissionsResult], to notify us of permission results. */
    fun notifyPermissionsChanged()

    /** Emits if the specified permissions are granted. */
    @CheckResult
    fun permissionsGranted(vararg permissions: String): Observable<Boolean> = permissionsGranted(permissions.toList())

    /** Emits if the specified permissions are granted. */
    @CheckResult
    fun permissionsGranted(permissions: Iterable<String>): Observable<Boolean>

    /** Emits the value of [ActivityCompat.shouldShowRequestPermissionRationale]. */
    @CheckResult
    fun shouldShowRequestPermissionRationale(permission: String): Observable<Boolean>

    /** Emits the state of the specified permission. */
    @CheckResult
    fun state(permission: String): Observable<PermissionState>
}

/** Indicates the state of a permission granted/denied status. */
enum class PermissionState {
    /** Permission was never granted, or was explicitly denied. */
    DENIED,
    /** Permission was denied, with "Don't ask again" checked. */
    DENIED_PERMANENTLY,
    /** Permission was granted. */
    GRANTED
}

@Singleton
internal class RealPermissions @Inject constructor(private val application: Application) : Permissions {

    private val receivedResultsPrefs =
        application.getSharedPreferences("com.nextfaze.daggie.permissions.receivedResults", MODE_PRIVATE)
    private val resultsSubject = PublishSubject.create<Any>()
    private val activityResumeEvents = PublishSubject.create<Any>()
    private val activityStartStopEvents = PublishSubject.create<Any>()
    private val activityStack = LinkedList<FragmentActivity>()
    private val topStartedActivity = activityStartStopEvents.startWith(Unit).map { activityStack.peek().toOptional() }

    val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            // Add a headless fragment to every activity that relays permission results
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.beginTransaction()
                    .add(PermissionCallbackFragment(), FRAGMENT_TAG)
                    .commit()
            }
        }

        // Post resume events to give us cues to reevaluate permission status
        override fun onActivityResumed(activity: Activity?) = activityResumeEvents.onNext(Unit)

        override fun onActivityStarted(activity: Activity) {
            // Track the top started activity, because we'll need an Activity instance to check certain things
            if (activity is FragmentActivity) {
                activityStack.push(activity)
                activityStartStopEvents.onNext(Unit)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity is FragmentActivity) {
                activityStack.remove(activity)
                activityStartStopEvents.onNext(Unit)
            }
        }

        override fun onActivityPaused(activity: Activity?) {
        }

        override fun onActivityDestroyed(activity: Activity?) {
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        }
    }

    // Record permissions we've received results for
    fun run(): Completable = permissionResultSubject
        .doOnNext { receivedResultsPrefs.edit().putBoolean(it, true).apply() }
        .ignoreElements()

    override fun requestPermissions(permissions: Iterable<String>): Completable = topStartedActivity
        .filterPresent()
        .take(1)
        .flatMapCompletable { it.requestPermissions(permissions) }

    private fun FragmentActivity.requestPermissions(permissions: Iterable<String>) = Completable.fromAction {
        if (!arePermissionsGranted(permissions)) {
            requestPermissionViaFragment(permissions.toList().toTypedArray())
        }
    }

    override fun notifyPermissionsChanged() = resultsSubject.onNext(Unit)

    override fun permissionsGranted(permissions: Iterable<String>): Observable<Boolean> = activityResumeEvents
        .map { Unit }
        .startWith(Unit)
        .map { application.arePermissionsGranted(permissions) }
        .distinctUntilChanged()

    override fun shouldShowRequestPermissionRationale(permission: String): Observable<Boolean> = permissionResultSubject
        .cast(Any::class.java)
        .startWith(Unit)
        .switchMap {
            topStartedActivity.filterPresent()
                .map { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) }
                .distinctUntilChanged()
        }

    override fun state(permission: String): Observable<PermissionState> = Observable
        .mergeArray(permissionResultSubject, activityResumeEvents)
        .cast(Any::class.java)
        .startWith(Unit)
        .switchMap {
            topStartedActivity
                .filterPresent()
                .take(1)
                .map { activity ->
                    state(
                        hasResult = receivedResultsPrefs.contains(permission),
                        isGranted = ContextCompat.checkSelfPermission(application, permission) == PackageManager.PERMISSION_GRANTED,
                        shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    )
                }
        }
        .distinctUntilChanged()
        .replay(1)
        .refCount()

    // https://stackoverflow.com/questions/30719047
    // shouldShowRationalePermissionRationale() method returns false in three cases:
    // 1. If we call this method very first time before asking permission.
    // 2. If user selects "Don't ask again" and deny permission.
    // 3. If the device policy prohibits the app from having that permission
    private fun state(hasResult: Boolean, isGranted: Boolean, shouldShowRationale: Boolean) = when {
        isGranted -> PermissionState.GRANTED
        hasResult && !shouldShowRationale -> PermissionState.DENIED_PERMANENTLY
        else -> PermissionState.DENIED
    }
}

private val permissionResultSubject = PublishSubject.create<String>()

internal fun FragmentActivity.requestPermissionViaFragment(permissions: Array<out String>) = supportFragmentManager
    .findFragmentByTag(FRAGMENT_TAG)!!
    .requestPermissions(permissions, PermissionCallbackFragment.REQUEST_CODE)

/** Used to get a callback when we got a permission result. */
internal class PermissionCallbackFragment : Fragment() {

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) permissions.forEach(permissionResultSubject::onNext)
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}

private fun Context.arePermissionsGranted(permissions: Iterable<String>) =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

private const val FRAGMENT_TAG = "com.nextfaze.daggie.permissions.fragment"
