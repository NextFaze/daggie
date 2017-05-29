package com.nextfaze.daggie.logback

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import com.nextfaze.daggie.Early
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.slf4j.logger
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Alias for a Logback appender. */
typealias LogbackAppender = AppenderBase<ILoggingEvent>

/**
 * Provides bindings that initialize Logback logging.
 *
 * Users are required to provide a binding for `@RootLevel Level`, indicating the default root log level to be used.
 * It's recommended this be based on the debug status of the build:
 *
 *     @Provides @RootLevel fun rootLevel() = if (BuildConfig.DEBUG) Level.TRACE else Level.INFO
 *
 * By default, a logcat appender is configured. Additional appenders can be configured by providing set bindings for
 * [LogbackAppender].
 * @see RootLevel
 * @see Level
 * @see LogbackAppender
 */
@Module class LogbackModule {
    @Provides @ElementsIntoSet
    internal fun appenders() = emptySet<LogbackAppender>()

    @Provides @Singleton
    internal fun loggerContext() = androidLogbackLoggerContext()

    /** Initializes just a logcat appender. Performed early to ensure everything gets logged. */
    @Provides @IntoSet @Singleton @Early
    internal fun logCatInitializer(
            loggerContext: LoggerContext,
            logback: Logback,
            logCatAppender: LogCatAppender
    ): Initializer<Application> = {
        Thread.setDefaultUncaughtExceptionHandler(debugLoggingUncaughtExceptionHandler(it, Thread.getDefaultUncaughtExceptionHandler()))
        logback.addAppender(logCatAppender.apply {
            context = loggerContext
            start()
        })
    }

    /** Initializes all other appends, and the configuration stored in assets. */
    @Provides @IntoSet @Singleton
    internal fun initAppenders(
            app: Application,
            logback: Logback,
            appenders: @JvmSuppressWildcards Set<LogbackAppender>
    ): Initializer<Application> = {
        appenders.forEach { logback.addAppender(it) }
        logback.loadConfig(app, "logback.xml")
    }
}

@Singleton
internal class Logback @Inject constructor(private val loggerContext: LoggerContext, @RootLevel rootLevel: Level) {
    private val rootLogger: Logger =
            (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).apply { level = rootLevel }

    fun addAppender(appender: Appender<ILoggingEvent>) = rootLogger.addAppender(appender)

    fun loadConfig(ctx: Context, assetPath: String) {
        try {
            ctx.assets.open(assetPath).use {
                JoranConfigurator().apply {
                    context = loggerContext
                    doConfigure(it)
                }
            }
        } catch (t: Throwable) {
            Log.e(this::class.java.name, "Error loading configuration from asset", t)
        }
    }
}

/** Prevents the use of [ClassLoader.getResourceAsStream]. Call this before [LoggerFactory.getILoggerFactory]. */
private fun applyLoggerContextPerformanceFix() {
    val file = File.createTempFile("logback", "xml")!!
    System.setProperty("logback.configurationFile", file.canonicalPath)
}

private fun androidLogbackLoggerContext(): LoggerContext {
    applyLoggerContextPerformanceFix()
    return LoggerFactory.getILoggerFactory() as LoggerContext
}

/**
 * This will allow us to see the full stack track in LogCat.
 * The default LogCat output `E/AndroidRuntime﹕ FATAL EXCEPTION: main ...` gets cut off when > ~4k characters.
 * However we only do this in Debug as calling `log.error()` may also send the logs to other crash logging services.
 * But as an uncaught exception, this has already been done (thus they will be doubled - but for debuggable builds only)
 * @see ApplicationInfo.FLAG_DEBUGGABLE
 */
private fun debugLoggingUncaughtExceptionHandler(context: Context, wrapped: Thread.UncaughtExceptionHandler) =
        object : Thread.UncaughtExceptionHandler {

            private val log = logger()

            override fun uncaughtException(thread: Thread, ex: Throwable) {
                if (context.isDebuggable) {
                    log.error("FATAL EXCEPTION", ex)
                }
                wrapped.uncaughtException(thread, ex)
            }
        }

private val Context.isDebuggable get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0