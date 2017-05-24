package com.nextfaze.daggie.rxjava

import dagger.Module
import dagger.Provides
import rx.Scheduler
import rx.plugins.RxJavaSchedulersHook
import rx.plugins.RxJavaSchedulersHook.createComputationScheduler
import rx.plugins.RxJavaSchedulersHook.createIoScheduler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/** Provides production-suitable RxJava [schedulers][Scheduler]. */
@Module class RxJavaSchedulerModule {
    /**
     * The default name is "RxIoScheduler-", however when using `systrace` thread names are truncated at 16 characters.
     * This results in threads > 9 (i.e. 2 digits) being truncated with only the first digit visible.
     * Hence this is here just to shorten the io thread names.
     * @see [RxJavaSchedulersHook.createIoScheduler]
     * @see [RxJavaSchedulersHook.createComputationScheduler]
     */
    @Provides @Singleton @Io fun ioScheduler(): Scheduler = createIoScheduler(threadFactory("RxIo-"))

    @Provides @Singleton @Computation fun computationScheduler(): Scheduler =
            createComputationScheduler(threadFactory("RxComp-"))
}

private fun threadFactory(prefix: String) = object : ThreadFactory {

    private val id = AtomicLong()

    override fun newThread(runnable: Runnable) =
            Thread(runnable, prefix + id.incrementAndGet()).apply { isDaemon = true }
}