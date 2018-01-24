package com.nextfaze.daggie.threeten

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_LOCALE_CHANGED
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import android.content.Intent.ACTION_TIME_CHANGED
import android.content.IntentFilter
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import org.threeten.bp.ZoneId
import java.util.*
import javax.inject.Singleton

/** Provides bindings that initialize the ThreeTen Android Backport library. */
@Module
class ThreeTenModule {
    @Provides @IntoSet
    internal fun initializer() = Ordered<Initializer<Application>>(0) { AndroidThreeTen.init(it) }

    @Provides @Singleton
    internal fun zoneIdFlowable(context: Context): Flowable<ZoneId> =
            context.receiverValue(ACTION_TIMEZONE_CHANGED, ACTION_TIME_CHANGED) { ZoneId.systemDefault() }

    @Provides @Singleton
    internal fun zoneIdObservable(zoneId: Flowable<ZoneId>): Observable<ZoneId> = zoneId.toObservable()

    @Provides @Singleton
    internal fun localeFlowable(context: Context): Flowable<Locale> =
            context.receiverValue(ACTION_LOCALE_CHANGED) { Locale.getDefault() }

    @Provides @Singleton
    internal fun localeObservable(locale: Flowable<Locale>): Observable<Locale> = locale.toObservable()
}

private fun <T> Context.receiverValue(vararg actions: String, currentValue: Context.() -> T): Flowable<T> =
        Flowable.create<T>({ emitter ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) = emitter.onNext(currentValue())
            }
            registerReceiver(receiver, IntentFilter().apply { actions.forEach { addAction(it) } })
            emitter.setCancellable { unregisterReceiver(receiver) }
            emitter.onNext(currentValue())
        }, BackpressureStrategy.LATEST).distinctUntilChanged()
