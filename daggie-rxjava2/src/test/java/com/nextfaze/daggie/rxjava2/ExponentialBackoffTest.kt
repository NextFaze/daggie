package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test
import java.util.concurrent.TimeUnit

class ExponentialBackoffTest {

    private val scheduler = TestScheduler()

    private val exponentialStrategy = BackoffStrategy.Exponential(
        maxAttempts = 3,
        maxDelay = 10,
        maxDelayUnit = TimeUnit.SECONDS
    )

    private val singleRetryStrategy = BackoffStrategy.Exponential(maxAttempts = 1)

    @Test(expected = IllegalArgumentException::class)
    fun `given strategy with zero attempts, should throw`() {
        BackoffStrategy.Exponential(maxAttempts = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given strategy with negative attempts, should throw`() {
        BackoffStrategy.Exponential(maxAttempts = 0)
    }

    @Test fun `given strategy with max attempts, should not emit more than max attempts`() {
        test(exponentialStrategy) {
            scheduler.advanceTimeBy(Long.MAX_VALUE, TimeUnit.SECONDS)
            assertValueCount(exponentialStrategy.maxAttempts)
        }
    }

    @Test fun `given strategy with max delay, should not delay more than max delay`() {
        test(exponentialStrategy) {
            scheduler.advanceTimeBy(10, TimeUnit.SECONDS)
            assertValueCount(3)
        }
    }

    @Test fun `given large number of attempts, should not internally overflow`() {
        test(
            BackoffStrategy.Exponential(
                maxAttempts = 500,
                maxDelay = 10,
                maxDelayUnit = TimeUnit.SECONDS
            )
        ) {
            // Not enough time has elapsed for any emissions to have occurred, unless it internally overflowed
            // and started scheduling with <= 0 delays. That would trigger 1+ erroneous emissions.
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            assertNoValues()
        }
    }

    @Test fun `given strategy with max int value attempts, should not error`() {
        test(BackoffStrategy.Exponential(maxAttempts = Int.MAX_VALUE)) {
            assertNoErrors()
        }
    }

    @Test fun `given predicate, should emit if exception passes predicate`() {
        val exception = RuntimeException()
        val observer = Flowable.just(exception)
            .backoffRetry(strategy = singleRetryStrategy,
                scheduler = scheduler,
                predicate = { it is RuntimeException }
            )
            .test()
        scheduler.advanceTimeBy(Long.MAX_VALUE, TimeUnit.SECONDS)
        observer.assertValueCount(1)
    }

    @Test fun `given predicate, should not emit if exception fails predicate`() {
        val exception = Exception()
        Flowable.just(exception)
            .backoffRetry(strategy = singleRetryStrategy,
                scheduler = scheduler,
                predicate = { it !is RuntimeException }
            )
            .test()
            .assertNoValues()
    }

    private fun test(
        strategy: BackoffStrategy,
        errors: Flowable<Throwable> = neverEndingErrors(),
        apply: TestSubscriber<Any>.() -> Unit
    ) = errors.backoffRetry(strategy = strategy, scheduler = scheduler).test().apply(apply)

    private fun neverEndingErrors() = Flowable.range(0, Int.MAX_VALUE).map { Throwable() }
}
