package com.nextfaze.daggie.rxjava2

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Provides plain, production-suitable RxJava 2 [Scheduler] bindings.
 *
 * This module also uses shorter thread names than the default, to avoid truncation by `systrace`.
 */
@Module class RxJava2SchedulerModule {
    @Provides @Singleton @Io
    internal fun ioScheduler() = RxJavaPlugins.createIoScheduler(threadFactory("RxIo-"))

    @Provides @Singleton @Computation
    internal fun computationScheduler() = RxJavaPlugins.createComputationScheduler(threadFactory("RxComp-"))

    @Provides @Singleton @IntoSet
    internal fun initializer(@Io ioScheduler: Scheduler, @Computation computationScheduler: Scheduler) =
            Ordered<Initializer<Application>>(0) {
                RxJavaPlugins.setComputationSchedulerHandler { computationScheduler }
                RxJavaPlugins.setIoSchedulerHandler { ioScheduler }
            }
}

private fun threadFactory(prefix: String) = object : ThreadFactory {

    private val id = AtomicLong()

    override fun newThread(runnable: Runnable) =
            Thread(runnable, prefix + id.incrementAndGet()).apply { isDaemon = true }
}
