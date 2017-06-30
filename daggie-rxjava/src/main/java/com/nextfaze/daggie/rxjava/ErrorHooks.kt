package com.nextfaze.daggie.rxjava

import com.nextfaze.daggie.slf4j.e
import com.nextfaze.daggie.slf4j.logger
import rx.Completable
import rx.CompletableSubscriber
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.SingleSubscriber
import rx.Subscriber
import rx.Subscription
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.functions.Action0
import rx.plugins.RxJavaHooks
import java.util.*
import java.util.Collections.newSetFromMap

internal typealias ErrorHandler = (Throwable) -> Unit

internal fun initErrorHooks(onError: ErrorHandler = { log.e(it) { "RxJava Error" } }) {
    /**
     * RxJava has very weird semantics and inconsistencies for error handling.
     * Unfortunately there are circumstances where an error handler will be called with the same error multiple times,
     * hence the weak set (using map maker which does identity comparison).
     * *Frequently occurs when using a [Single] with a [Scheduler].*
     */
    val logged: MutableSet<Throwable> = newSetFromMap(WeakHashMap())
    val deduplicatingErrorHandler: ErrorHandler = {
        if (logged.add(it)) {
            onError(it)
        }
    }
    RxJavaHooks.setOnError(deduplicatingErrorHandler)
    RxJavaHooks.setOnObservableCreate { SafeObservableOnSubscribe(deduplicatingErrorHandler, it) }
    RxJavaHooks.setOnSingleCreate { SafeSingleOnSubscribe(deduplicatingErrorHandler, it) }
    RxJavaHooks.setOnCompletableCreate { SafeCompletableOnSubscribe(deduplicatingErrorHandler, it) }
    RxJavaHooks.setOnScheduleAction { SafeAction0(deduplicatingErrorHandler, it) }
    RxAndroidPlugins.getInstance().registerSchedulersHook(RxSchedulersHookAndroid(deduplicatingErrorHandler))
}

internal fun clearErrorHooks() {
    RxJavaHooks.reset()
    RxAndroidPlugins.getInstance().reset()
}

private inline fun <T> safely(onError: ErrorHandler, body: () -> T) = try {
    body()
} catch (e: Throwable) {
    onError(e)
}


private val log = logger("RxJavaErrorHooks")

private class SafeObservableOnSubscribe(
        private val onError: ErrorHandler,
        private val onSubscribe: Observable.OnSubscribe<*>
) : Observable.OnSubscribe<Any> {
    override fun call(subscriber: Subscriber<in Any>) {
        // Don't wrap this call, because if we do, errors aren't delivered to the onError callback as per the contract
        onSubscribe.call(SafeObservableSubscriber(onError, subscriber))
    }

    private class SafeObservableSubscriber<T>(
            private val onError: ErrorHandler,
            private val subscriber: Subscriber<in T>
    ) : Subscriber<T>(subscriber) {
        override fun onNext(t: T?) {
            safely(onError) { subscriber.onNext(t) }
        }

        override fun onCompleted() {
            safely(onError) { subscriber.onCompleted() }
        }

        override fun onError(e: Throwable) {
            safely(onError) { subscriber.onError(e) }
        }
    }
}

private class SafeSingleOnSubscribe(
        private val onError: ErrorHandler,
        private val onSubscribe: Single.OnSubscribe<*>
) : Single.OnSubscribe<Any> {
    override fun call(singleSubscriber: SingleSubscriber<in Any>) {
        onSubscribe.call(SafeSingleSubscriber(onError, singleSubscriber))
    }

    private class SafeSingleSubscriber<T>(
            private val onError: ErrorHandler,
            private val singleSubscriber: SingleSubscriber<in T>
    ) : SingleSubscriber<T>() {
        override fun onSuccess(value: T?) {
            safely(onError) { singleSubscriber.onSuccess(value) }
        }

        override fun onError(error: Throwable) {
            safely(onError) { singleSubscriber.onError(error) }
        }
    }
}

private class SafeCompletableOnSubscribe(
        private val onError: ErrorHandler,
        private val onSubscribe: Completable.OnSubscribe
) : Completable.OnSubscribe {
    override fun call(completableSubscriber: CompletableSubscriber) {
        safely(onError) { onSubscribe.call(SafeCompletableSubscriber(onError, completableSubscriber)) }
    }

    private class SafeCompletableSubscriber(
            private val onError: ErrorHandler,
            private val completableSubscriber: CompletableSubscriber
    ) : CompletableSubscriber {
        override fun onSubscribe(subscription: Subscription) {
            safely(onError) { completableSubscriber.onSubscribe(subscription) }
        }

        override fun onError(t: Throwable) {
            safely(onError) { completableSubscriber.onError(t) }
        }

        override fun onCompleted() {
            safely(onError) { completableSubscriber.onCompleted() }
        }
    }
}

private class SafeAction0(private val onError: ErrorHandler, private val action0: Action0) : Action0 {
    override fun call() {
        safely(onError) {
            action0.call()
        }
    }
}

private class RxSchedulersHookAndroid(private val onError: ErrorHandler) : RxAndroidSchedulersHook() {
    override fun onSchedule(action: Action0): Action0 = SafeAction0(onError, action)
}