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
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
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
 * Users must provide a `LogbackConfig` binding, to configure properties such as the root log level and config asset
 * path.
 *
 * By default, a logcat appender is configured. Additional appenders can be configured by providing set bindings for
 * [LogbackAppender].
 * @see Level
 * @see LogbackAppender
 */
@Module class LogbackModule {
    @Provides @Singleton
    internal fun logback(context: Context, loggerContext: LoggerContext, config: LogbackConfig) =
            Logback(loggerContext, config.rootLevel ?: defaultRootLevel(context))

    @Provides @ElementsIntoSet
    internal fun appenders() = emptySet<LogbackAppender>()

    @Provides @Singleton
    internal fun loggerContext() = androidLogbackLoggerContext()

    /** Initializes just a logcat appender. Performed early to ensure everything gets logged. */
    @Provides @IntoSet @Singleton
    internal fun logCatInitializer(
            loggerContext: LoggerContext,
            logback: Logback,
            logCatAppender: LogCatAppender
    ) = Ordered<Initializer<Application>>(0, {
        logback.addAppender(logCatAppender.apply {
            context = loggerContext
            start()
        })
    })

    /** Initializes all other appends, and the configuration stored in assets. */
    @Provides @IntoSet @Singleton
    internal fun initAppenders(
            app: Application,
            logback: Logback,
            config: LogbackConfig,
            appenders: @JvmSuppressWildcards Set<LogbackAppender>
    ): Initializer<Application> = {
        appenders.forEach { logback.addAppender(it) }
        if (config.configAssetPath != null) logback.loadConfig(app, config.configAssetPath)
    }
}

internal class Logback @Inject constructor(private val loggerContext: LoggerContext, rootLevel: Level) {
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

private val Context.isDebuggable get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

private fun defaultRootLevel(context: Context) = if (context.isDebuggable) Level.TRACE else Level.INFO