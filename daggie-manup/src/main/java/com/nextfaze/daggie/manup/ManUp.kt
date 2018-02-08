package com.nextfaze.daggie.manup

import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.support.v4.app.FragmentActivity
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nextfaze.daggie.Foreground
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.BehaviorSubject
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Provides bindings that configure "mandatory updates" (aka ManUp). This triggers a dialog that can suggest or
 * force the user to update the app version, based on a remote JSON config file.
 *
 * Users of this module must provide the following bindings:
 * * [OkHttpClient]
 * * [ManUpConfig]
 * * [Foreground] `Observable<Boolean>`
 */
@Module class ManUpModule {
    @Provides @IntoSet internal fun initializer(
        httpClient: OkHttpClient,
        config: ManUpConfig,
        @Foreground foreground: Observable<Boolean>
    ): Initializer<Application> = { initManUp(it, httpClient, foreground, config) }
}

private const val RETRY_MAX_DELAY = 10L
private val RETRY_MAX_DELAY_UNIT = MINUTES

// TODO: Parse config JSON manually to eliminate Gson, AutoValue, and Retrofit dependencies.

private fun initManUp(
    application: Application,
    httpClient: OkHttpClient,
    foreground: Observable<Boolean>,
    manUpConfig: ManUpConfig
) {
    // Use our own Gson
    val gson = GsonBuilder()
        .registerTypeAdapterFactory(PlatformUnifiedConfigTypeAdapterFactory())
        .create()!!

    // Create Retrofit API
    val api = Retrofit.Builder()
        .callFactory(httpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(io()))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl("http://example.com") // Dummy value; not actually used
        .build()
        .create(ManUpApi::class.java)!!

    // Config will be stored in prefs
    val configPref = RxSharedPreferences.create(application.getSharedPreferences("com.nextfaze.manup", MODE_PRIVATE))
        .getObject("config", Config(), gson.preferenceConverter<Config>())

    // Load remote config into prefs
    val syncConfigWithApi = api.config(manUpConfig.url).doOnSuccess { configPref.set(it) }.toCompletable()!!

    // Emits config pref values, which are synchronized with the API upon each subscription
    val syncConfig = Flowable.merge(
        syncConfigWithApi.toFlowable(),
        configPref.asObservable().toFlowable(BackpressureStrategy.LATEST)
    )!!

    // Tracks if the user has been shown a recommended update
    var updateShown = false

    // Periodically update config and evaluate it, as long as app is foregrounded
    val configSubject: BehaviorSubject<Config> = BehaviorSubject.create<Config>()
    Flowable.interval(0L, manUpConfig.pollingInterval, manUpConfig.pollingIntervalUnit)
        .switchMap { syncConfig }
        .retryWhen { it.exponentialBackoff(maxDelay = RETRY_MAX_DELAY, maxDelayUnit = RETRY_MAX_DELAY_UNIT) }
        .observeOn(mainThread())
        .takeWhile(foreground.toFlowable(BackpressureStrategy.LATEST))
        .distinctUntilChanged()
        .subscribe { configSubject.onNext(it) }

    application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityResumed(activity: Activity) {
            configSubject.takeUntil(activity.pauses()).subscribe {
                if (activity is FragmentActivity) {
                    // Show the update dialog, or update an existing one.
                    // Don't show it again if user has already seen an optional update dialog.
                    val fragmentManager = activity.supportFragmentManager!!
                    when (it.check(application.versionCode)) {
                        Result.MAINTENANCE_MODE, Result.UPDATE_REQUIRED -> ManUpDialogFragment.show(fragmentManager, it)
                        Result.UPDATE_RECOMMENDED -> if (!updateShown) {
                            updateShown = true
                            ManUpDialogFragment.show(fragmentManager, it)
                        } else {
                            ManUpDialogFragment.update(fragmentManager, it)
                        }
                        else -> ManUpDialogFragment.dismiss(fragmentManager)
                    }
                }
            }
        }
    })
}

/** Provides access to remote ManUp config URL. */
internal interface ManUpApi {
    /** Loads the remote [Config], ensuring any cached response is validated against the origin server before use. */
    @GET()
    @Headers("Cache-Control: max-age=0, public")
    fun config(@Url url: HttpUrl): Single<Config>
}

internal enum class Result {
    /** User may continue using the app. */
    OK,
    /** User is locked out of the app. */
    MAINTENANCE_MODE,
    /** User is forced to update the app. */
    UPDATE_REQUIRED,
    /** User is recommended to update the app, but may continue using it. */
    UPDATE_RECOMMENDED
}

/** Compares the specified version code again this configuration, returning what to do next. */
internal fun Config?.check(installedVersionCode: Int) = when {
    this == null -> Result.OK
    maintenanceMode -> Result.MAINTENANCE_MODE
    installedVersionCode in minimumVersion..(currentVersion - 1) -> Result.UPDATE_RECOMMENDED
    installedVersionCode < minimumVersion -> Result.UPDATE_REQUIRED
    else -> Result.OK
}

/** Returns a Gson [Preference.Converter] for the inferred type [T]. */
private inline fun <reified T : Any> Gson.preferenceConverter() = object : Preference.Converter<T> {
    override fun serialize(value: T) = toJson(value)
    override fun deserialize(serialized: String) = fromJson(serialized, T::class.java)!!
}
