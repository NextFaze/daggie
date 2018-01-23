package com.nextfaze.daggie.permissions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.support.annotation.CheckResult
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton

/** Provides bindings for initializing the [Permissions] permissions manager. */
@Module
class PermissionsModule {
    @Provides @IntoSet
    internal fun initializer(@Suppress("UNUSED_PARAMETER") permissions: Permissions):
            Initializer<Application> = {
        // Just initialize the object graph
    }

    @Provides @Singleton
    internal fun permissions(application: Application): Permissions = RealPermissions(application)
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
}

private class RealPermissions(private val application: Application) : Permissions {

    private val activity = application.topResumedActivity().replay(1)

    private val resultsSubject = PublishSubject.create<Any>()

    init {
        activity.connect()
    }

    override fun requestPermissions(permissions: Iterable<String>): Completable = activity.filterPresent()
            .take(1)
            .flatMapCompletable { it.requestPermissions(permissions) }

    override fun notifyPermissionsChanged() = resultsSubject.onNext(Unit)

    override fun permissionsGranted(permissions: Iterable<String>): Observable<Boolean> = application.activityResumes()
            .map { Unit }
            .startWith(Unit)
            .map { application.arePermissionsGranted(permissions) }
            .distinctUntilChanged()

    override fun shouldShowRequestPermissionRationale(permission: String): Observable<Boolean> =
            activity.filterPresent()
                    .map { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) }
                    .distinctUntilChanged()
}

private fun Activity.requestPermissions(permissions: Iterable<String>) = Completable.fromCallable {
    if (!arePermissionsGranted(permissions)) {
        ActivityCompat.requestPermissions(this, permissions.toList().toTypedArray(), PERMISSION_REQUEST_CODE)
    }
}

private fun Context.arePermissionsGranted(permissions: Iterable<String>) =
        permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

/** An arbitrary request code that hopefully doesn't collide with any application codes. */
private const val PERMISSION_REQUEST_CODE = 5923
