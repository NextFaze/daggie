package com.nextfaze.daggie.slf4j

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/** Provides bindings to initialize SLF4J, including logging uncaught exceptions. */
@Module class Slf4jModule {
    @Provides @IntoSet @Singleton
    internal fun initializer() = Ordered<Initializer<Application>>(0) {
        Thread.setDefaultUncaughtExceptionHandler(debugLoggingUncaughtExceptionHandler(it,
                Thread.getDefaultUncaughtExceptionHandler()))
    }
}

/**
 * This will allow us to see the full stack track in LogCat.
 * The default LogCat output `E/AndroidRuntime﹕ FATAL EXCEPTION: main ...` gets cut off when > ~4k characters.
 * However we only do this in Debug as calling `log.error()` may also send the logs to other crash logging services.
 * But as an uncaught exception, this has already been done (thus they will be doubled - but for debuggable builds only)
 * @see ApplicationInfo.FLAG_DEBUGGABLE
 */
private fun debugLoggingUncaughtExceptionHandler(context: Context, wrapped: Thread.UncaughtExceptionHandler?) =
        object : Thread.UncaughtExceptionHandler {

            private val log = logger()

            override fun uncaughtException(thread: Thread, ex: Throwable) {
                if (context.isDebuggable) {
                    log.error("FATAL EXCEPTION", ex)
                }
                wrapped?.uncaughtException(thread, ex)
            }
        }

private val Context.isDebuggable get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
