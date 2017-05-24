package com.nextfaze.daggie.manup

import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Parcel
import android.support.v4.app.FragmentActivity
import com.f2prateek.rx.preferences.Preference
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapterFactory
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.jakewharton.rxrelay.BehaviorRelay
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import rx.Emitter
import rx.Observable
import rx.Observable.interval
import rx.Observable.merge
import rx.Scheduler
import rx.Single
import rx.android.schedulers.AndroidSchedulers.mainThread
import rx.schedulers.Schedulers
import rx.schedulers.Schedulers.io
import java.lang.Math.pow
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

private const val UPDATE_INTERVAL = 10L
private val UPDATE_UNIT = MINUTES
private const val RETRY_MAX_DELAY = 10L
private val RETRY_MAX_DELAY_UNIT = MINUTES

// TODO: Parse config JSON manually to eliminate Gson dependency.

// TODO: Request config URL async manually to eliminate Retrofit dependency.

internal fun initManUp(
        application: Application,
        httpClient: OkHttpClient,
        foreground: Observable<Boolean>,
        @ManUpConfigUrl configUrl: String
) {
    // Use our own Gson
    val gson = GsonBuilder()
            .registerTypeAdapterFactory(ManUpAutoValueTypeAdapterFactory.create())
            .registerTypeAdapter(HttpUrl::class.java, HttpUrlTypeAdapter())
            .create()!!

    // Parse HTTP URL from caller-supplied URL string
    val url = HttpUrl.parse(configUrl)!!

    // Create Retrofit API
    val api = Retrofit.Builder()
            .callFactory(httpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(io()))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("http://beac.onl/") // Dummy value; not actually used
            .build()
            .create(ManUpApi::class.java)!!

    // Config will be stored in prefs
    val configPref = RxSharedPreferences.create(application.getSharedPreferences("com.nextfaze.manup", MODE_PRIVATE))
            .getObject("config", null, gson.preferenceAdapter<ManUpConfig?>())

    // Load remote config into prefs
    val syncConfigWithApi = api.config(url).doOnSuccess { configPref.set(it) }.toCompletable()

    // Emits config pref values, which are synchronized with the API upon each subscription
    val syncConfig = merge(syncConfigWithApi.toObservable(), configPref.asObservable())

    // Tracks if the user has been shown a recommended update
    var updateShown = false

    // Periodically update config and evaluate it, as long as app is foregrounded
    val configRelay = BehaviorRelay.create<ManUpConfig>()
    interval(0L, UPDATE_INTERVAL, UPDATE_UNIT)
            .switchMap { syncConfig }
            .retryWhen { it.exponentialBackoff(maxDelay = RETRY_MAX_DELAY, maxDelayUnit = RETRY_MAX_DELAY_UNIT) }
            .takeWhile(foreground)
            .distinctUntilChanged()
            .observeOn(mainThread())
            .subscribe(configRelay)

    application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityResumed(activity: Activity) {
            configRelay.takeUntil(activity.pauses()).subscribe {
                if (activity is FragmentActivity) {
                    // Show the update dialog, or update an existing one.
                    // Don't show it again if user has already seen an optional update dialog.
                    val fragmentManager = activity.supportFragmentManager
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
    /** Loads the remote [ManUpConfig]. */
    @GET()
    fun config(@Url url: HttpUrl): Single<ManUpConfig>
}

/** Compares the specified version code again this configuration, returning what to do next. */
internal fun ManUpConfig?.check(versionCode: Int) = when {
    this == null -> Result.OK
    versionCode in minimumVersion..(currentVersion - 1) -> Result.UPDATE_RECOMMENDED
    versionCode < minimumVersion -> Result.UPDATE_REQUIRED
    else -> Result.OK
}

internal enum class Result {
    /** User may continue using the app. */
    OK,
    /** User is forced to update the app. */
    UPDATE_REQUIRED,
    /** User is recommended to update the app, but may continue using it. */
    UPDATE_RECOMMENDED
}

@GsonTypeAdapterFactory internal abstract class ManUpAutoValueTypeAdapterFactory : TypeAdapterFactory {
    companion object {
        @JvmStatic fun create(): TypeAdapterFactory = AutoValueGson_ManUpAutoValueTypeAdapterFactory()
    }
}

/** Returns a Gson [Preference.Adapter] for the inferred type [T]. */
private inline fun <reified T : Any?> Gson.preferenceAdapter() = object : Preference.Adapter<T> {
    override fun set(key: String, value: T, editor: SharedPreferences.Editor) {
        editor.putString(key, toJson(value))
    }

    override fun get(key: String, preferences: SharedPreferences): T =
            fromJson(preferences.getString(key, null), T::class.java)
}

internal class HttpUrlTypeAdapter : com.google.gson.TypeAdapter<HttpUrl?>(),
        com.ryanharter.auto.value.parcel.TypeAdapter<HttpUrl?> {
    override fun write(writer: JsonWriter, url: HttpUrl?) {
        writer.value(url.toString())
    }

    override fun read(reader: JsonReader) = reader.nextString().let { HttpUrl.parse(it) }!!

    override fun toParcel(url: HttpUrl?, parcel: Parcel) = parcel.writeString(url.toString())

    override fun fromParcel(parcel: Parcel): HttpUrl? = HttpUrl.parse(parcel.readString())
}

/** Emits pause events from this [Activity]. */
private fun Activity.pauses() = Observable.create<Unit>({ emitter ->
    val callbacks: SimpleActivityLifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityPaused(activity: Activity) {
            if (activity == this@pauses) {
                emitter.onNext(Unit)
            }
        }
    }
    application.registerActivityLifecycleCallbacks(callbacks)
    emitter.setCancellation { application.unregisterActivityLifecycleCallbacks(callbacks) }
}, Emitter.BackpressureMode.BUFFER)

/** Mirror the source observable while the specified observable's latest emitted value is true. */
private fun <T> Observable<T>.takeWhile(b: Observable<Boolean>): Observable<T> = b
        .distinctUntilChanged()
        .filter { it }
        .switchMap { takeUntil(b.distinctUntilChanged().filter { !it }) }

private const val EXPONENTIAL_BACKOFF_FIRST_RETRY_DELAY = 3000L

private fun <T : Throwable> Observable<T>.exponentialBackoff(
        maxAttempts: Int = Integer.MAX_VALUE,
        maxDelay: Long = Long.MAX_VALUE,
        maxDelayUnit: TimeUnit = SECONDS,
        scheduler: Scheduler = Schedulers.computation(),
        passthroughFilter: (Throwable) -> Boolean = { false }
): Observable<Long> = zipWith(Observable.range(0, maxAttempts - 1)) { e, attempt -> e to attempt }
        .flatMap {
            if (passthroughFilter.invoke(it.first)) Observable.error(it.first)
            else Observable.timer(maxDelayUnit.toMillis(maxDelay)
                    .coerceAtMost(pow(2.toDouble(), it.second.toDouble()).toLong()
                            * EXPONENTIAL_BACKOFF_FIRST_RETRY_DELAY), MILLISECONDS, scheduler)
        }