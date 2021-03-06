package com.nextfaze.daggie.manup

import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS

class ExponentialBackoffTest {

    private lateinit var scheduler: TestScheduler

    @Before fun setUp() {
        scheduler = TestScheduler()
    }

    @Test fun doesNotEmitMoreThanMaxAttempts() {
        val error = Flowable.range(0, Integer.MAX_VALUE).map { Exception() }
        val subscriber = TestSubscriber<Any>()
        error.exponentialBackoff(
                maxAttempts = 3,
                maxDelay = 1,
                maxDelayUnit = SECONDS,
                scheduler = scheduler
        ).subscribe(subscriber)
        scheduler.advanceTimeBy(Long.MAX_VALUE, SECONDS)
        subscriber.assertValueCount(3)
    }

    @Test fun doesNotDelayMoreThanMaxDelay() {
        val error = Flowable.range(0, Integer.MAX_VALUE).map { Exception() }
        val subscriber = TestSubscriber<Any>()
        error.exponentialBackoff(
                maxAttempts = 3,
                maxDelay = 10,
                maxDelayUnit = SECONDS,
                scheduler = scheduler
        ).subscribe(subscriber)
        scheduler.advanceTimeBy(10, SECONDS)
        subscriber.assertValueCount(3)
    }

    @Test fun largeNumberOfAttempts() {
        val error = Flowable.range(0, Integer.MAX_VALUE).map { Exception() }
        val subscriber = TestSubscriber<Any>()
        error.exponentialBackoff(
                maxAttempts = 500,
                maxDelay = 10,
                maxDelayUnit = SECONDS,
                scheduler = scheduler
        ).subscribe(subscriber)
        scheduler.advanceTimeBy(1, SECONDS)
        // Not enough time has elapsed for any emissions to have occurred, unless it internally overflowed
        // and started scheduling with <= 0 delays. That would trigger 1+ erroneous emissions.
        subscriber.assertValueCount(0)
    }
}
