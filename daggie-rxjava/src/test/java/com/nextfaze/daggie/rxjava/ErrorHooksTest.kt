package com.nextfaze.daggie.rxjava

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import rx.Completable
import rx.Observable
import rx.Single
import rx.exceptions.CompositeException
import rx.exceptions.OnCompletedFailedException
import rx.exceptions.OnErrorFailedException
import rx.exceptions.OnErrorNotImplementedException
import rx.functions.Action0
import rx.functions.Action1

class ErrorHooksTest {

    @get:Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var hook: ErrorHandler

    @Mock private lateinit var onError: Action1<Throwable>

    @Before fun setUp() = initErrorHooks(hook)

    @After fun tearDown() = clearErrorHooks()

    /* Observable */

    @Test fun observable_exceptionInOnSubscribeIsDeliveredToOnError() {
        Observable.unsafeCreate<Any> { throw ErrorHooksException() }.subscribe(Action1 {}, onError)
        verifyZeroHookInteractions()
        verifyOnErrorReceived<ErrorHooksException>()
    }

    // Can't seem to do anything about this exception slipping through
    @Ignore("Doesn't appear as if these exceptions can be caught") @Test
    fun observable_exceptionInOnSubscribeWithOnErrorNotImplementedIsHooked() {
        Observable.unsafeCreate<Any> { throw ErrorHooksException() }.subscribe()
        verifyHooked<OnErrorNotImplementedException>()
    }

    @Test fun observable_exceptionInOnNextIsDeliveredToOnError() {
        Observable.just(1).subscribe(Action1 { throw ErrorHooksException() }, onError)
        verifyZeroHookInteractions()
        verifyOnErrorReceived<ErrorHooksException>()
    }

    @Test fun observable_exceptionInOnNextWithOnErrorNotImplementedIsHooked() {
        Observable.just(1).subscribe { throw ErrorHooksException() }
        verifyHooked<OnErrorNotImplementedException>()
    }

    @Test fun observable_exceptionInOnCompletedIsHooked() {
        Observable.just(1).subscribe(Action1 {}, onError, Action0 { throw ErrorHooksException() })
        verifyHooked<OnCompletedFailedException>()
        verifyZeroOnErrorInteractions()
    }

    @Test fun observable_exceptionInOnErrorIsHooked() {
        Observable.error<Any>(RuntimeException()).subscribe({}, { throw ErrorHooksException() })
        verifyHooked<OnErrorFailedException>()
    }

    @Test fun observable_onErrorNotImplementedIsHooked() {
        Observable.error<Any>(ErrorHooksException()).subscribe()
        verifyHooked<OnErrorNotImplementedException>()
    }

    @Test fun observable_errorIsDeliveredToOnError() {
        Observable.error<Any>(ErrorHooksException()).subscribe(Action1 {}, onError)
        verifyOnErrorReceived<ErrorHooksException>()
    }

    /* Single */

    @Test fun single_exceptionInOnSubscribeIsDeliveredToOnError() {
        Single.create<Any> { throw ErrorHooksException() }.subscribe(Action1 {}, onError)
        verifyZeroHookInteractions()
        verifyOnErrorReceived<ErrorHooksException>()
    }

    // Just like Observable above, we can't catch OnErrorNotImplementedException in this case
    @Ignore("Doesn't appear as if these exceptions can be caught") @Test
    fun single_exceptionInOnSubscribeWithOnErrorNotImplementedIsHooked() {
        Single.create<Any> { throw ErrorHooksException() }.subscribe()
        verifyHooked<OnErrorNotImplementedException>()
    }

    @Test fun single_exceptionInOnSuccessIsDeliveredToOnError() {
        Single.just(1).subscribe(Action1 { throw ErrorHooksException() }, onError)
        // Single contract says only onSuccess or onFailure can be called, not both,
        // so we expect the hook to catch this one
        verifyHooked<ErrorHooksException>()
    }

    @Test fun single_exceptionInOnSuccessWithOnErrorNotImplementedIsHooked() {
        Single.just(1).subscribe { throw ErrorHooksException() }
        // Single differs from Observable and doesn't wrap the thrown exception in OnErrorNotImplementedException
        verifyHooked<ErrorHooksException>()
    }

    @Test fun single_exceptionInOnFailureIsHooked() {
        Single.error<Any>(RuntimeException()).subscribe({}, { throw ErrorHooksException() })
        // Single differs from Observable and doesn't wrap the thrown exception in OnErrorFailedException
        verifyHooked<ErrorHooksException>()
    }

    /* Completable */

    @Test fun completable_exceptionInOnSubscribeIsHooked() {
        Completable.create { throw ErrorHooksException() }.subscribe(Action0 {}, onError)
        // Completables don't seem to deliver exceptions to onError in this case,
        // so we rely on our hook to prevent crashing
        verifyHooked<ErrorHooksException>()
    }

    @Test fun completable_exceptionInOnSubscribeWithOnErrorNotImplementedIsHooked() {
        Completable.create { throw ErrorHooksException() }.subscribe()
        // Completables don't seem to deliver exceptions to onError in this case,
        // so we rely on our hook to prevent crashing
        verifyHooked<ErrorHooksException>()
    }

    @Test fun completable_errorIsDeliveredToOnError() {
        Completable.error(ErrorHooksException()).subscribe(Action0 {}, onError)
        verifyZeroHookInteractions()
        verifyOnErrorReceived<ErrorHooksException>()
    }

    @Test fun completable_exceptionInOnCompleted() {
        Completable.complete().subscribe(Action0 { throw ErrorHooksException() }, onError)
        // Completable is inconsistent with Single and Observable in that it allows calling of onCompleted AND onError
        verifyOnErrorReceived<ErrorHooksException>()
    }

    @Test fun completable_exceptionInOnCompletedWithOnErrorNotImplementedIsHooked() {
        Completable.complete().subscribe { throw ErrorHooksException() }
        verifyHooked<ErrorHooksException>()
    }

    @Test fun completable_exceptionInOnError() {
        Completable.error(RuntimeException()).subscribe({}, { throw ErrorHooksException() })
        // Completables combine the source exception with the newly thrown one
        verifyHooked<CompositeException>()
    }

    /* Utils */

    private inline fun <reified T : Throwable> verifyHooked() = verify(hook).invoke(any<T>())
    private inline fun <reified T : Throwable> verifyOnErrorReceived() = verify(onError).call(any<T>())
    private fun verifyZeroHookInteractions() = verifyZeroInteractions(hook)
    private fun verifyZeroOnErrorInteractions() = verifyZeroInteractions(onError)
}

class ErrorHooksException : Exception()
