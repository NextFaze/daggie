package com.nextfaze.daggie.logback

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LogCatAppender @Inject constructor(private val logCatLogger: LogCatLogger) : AppenderBase<ILoggingEvent>() {
    private val tagEncoder: PatternLayoutEncoder by lazy {
        PatternLayoutEncoder().apply {
            pattern = "%logger%nopex"
            context = this@LogCatAppender.context
            start()
        }
    }

    private val encoder: PatternLayoutEncoder by lazy {
        PatternLayoutEncoder().apply {
            pattern = "%msg%n%ex"
            context = this@LogCatAppender.context
            start()
        }
    }

    override fun append(event: ILoggingEvent) {
        val tag = tagEncoder.layout.doLayout(event)
        val msg = encoder.layout.doLayout(event)
        logCatLogger.log(tag, msg, event.level.toInt())
    }
}
