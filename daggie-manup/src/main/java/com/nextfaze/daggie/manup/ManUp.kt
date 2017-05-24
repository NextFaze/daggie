package com.nextfaze.daggie.manup

import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.support.v4.app.FragmentActivity
import com.f2prateek.rx.preferences.Preference
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import rx.Observable
import rx.Observable.interval
import rx.Observable.merge
import rx.Single
import rx.android.schedulers.AndroidSchedulers.mainThread
import rx.schedulers.Schedulers.io
import rx.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit.MINUTES

private const val UPDATE_INTERVAL = 10L
private val UPDATE_UNIT = MINUTES
private const val RETRY_MAX_DELAY = 10L
private val RETRY_MAX_DELAY_UNIT = MINUTES

// TODO: Parse config JSON manually to eliminate Gson, AutoValue, and Retrofit dependencies.

internal fun initManUp(
        application: Application,
        httpClient: OkHttpClient,
        foreground: Observable<Boolean>,
        config: ManUpConfig
) {
    // Use our own Gson
    val gson = GsonBuilder()
            .registerTypeAdapterFactory(ManUpAutoValueTypeAdapterFactory.create())
            .registerTypeAdapter(HttpUrl::class.java, HttpUrlTypeAdapter())
            .create()!!

    // Parse HTTP URL from caller-supplied URL string

    // Create Retrofit API
    val api = Retrofit.Builder()
            .callFactory(httpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(io()))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("http://example.com") // Dummy value; not actually used
            .build()
            .create(ManUpApi::class.java)!!

    // Config will be stored in prefs
    val configPref = RxSharedPreferences.create(application.getSharedPreferences("com.nextfaze.manup", MODE_PRIVATE))
            .getObject("config", null, gson.preferenceAdapter<Config?>())

    // Load remote config into prefs
    val syncConfigWithApi = api.config(config.url).doOnSuccess { configPref.set(it) }.toCompletable()!!

    // Emits config pref values, which are synchronized with the API upon each subscription
    val syncConfig = merge(syncConfigWithApi.toObservable(), configPref.asObservable())!!

    // Tracks if the user has been shown a recommended update
    var updateShown = false

    // Periodically update config and evaluate it, as long as app is foregrounded
    val configSubject: BehaviorSubject<Config> = BehaviorSubject.create<Config>()!!
    interval(0L, UPDATE_INTERVAL, UPDATE_UNIT)
            .switchMap { syncConfig }
            .retryWhen { it.exponentialBackoff(maxDelay = RETRY_MAX_DELAY, maxDelayUnit = RETRY_MAX_DELAY_UNIT) }
            .observeOn(mainThread())
            .takeWhile(foreground)
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
                        Result.UPDATE_REQUIRED -> ManUpDialogFragment.show(fragmentManager, it)
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
    /** Loads the remote [Config]. */
    @GET()
    fun config(@Url url: HttpUrl): Single<Config>
}

internal enum class Result {
    /** User may continue using the app. */
    OK,
    /** User is forced to update the app. */
    UPDATE_REQUIRED,
    /** User is recommended to update the app, but may continue using it. */
    UPDATE_RECOMMENDED
}

/** Compares the specified version code again this configuration, returning what to do next. */
internal fun Config?.check(versionCode: Int) = when {
    this == null -> Result.OK
    versionCode in minimumVersion..(currentVersion - 1) -> Result.UPDATE_RECOMMENDED
    versionCode < minimumVersion -> Result.UPDATE_REQUIRED
    else -> Result.OK
}

/** Returns a Gson [Preference.Adapter] for the inferred type [T]. */
private inline fun <reified T : Any?> Gson.preferenceAdapter() = object : Preference.Adapter<T> {
    override fun set(key: String, value: T, editor: SharedPreferences.Editor) {
        editor.putString(key, toJson(value))
    }

    override fun get(key: String, preferences: SharedPreferences): T =
            fromJson(preferences.getString(key, null), T::class.java)
}