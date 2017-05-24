package com.nextfaze.daggie.slf4j

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger

inline fun <reified T : Any> T.logger(name: String = T::class.java.name, nop: Boolean = false): Logger =
        if (nop) NOPLogger.NOP_LOGGER else LoggerFactory.getLogger(name)

fun logger(name: String): Logger = LoggerFactory.getLogger(name)

inline fun Logger.t(t: Throwable? = null, predicate: Boolean = true, body: () -> String) {
    if (predicate && isTraceEnabled) {
        trace(body.invoke(), t)
    }
}

inline fun Logger.d(t: Throwable? = null, predicate: Boolean = true, body: () -> String) {
    if (predicate && isDebugEnabled) {
        debug(body.invoke(), t)
    }
}

inline fun Logger.i(t: Throwable? = null, predicate: Boolean = true, body: () -> String) {
    if (predicate && isInfoEnabled) {
        info(body.invoke(), t)
    }
}

inline fun Logger.w(t: Throwable? = null, predicate: Boolean = true, body: () -> String) {
    if (predicate && isWarnEnabled) {
        warn(body.invoke(), t)
    }
}

inline fun Logger.e(t: Throwable? = null, predicate: Boolean = true, body: () -> String) {
    if (predicate && isErrorEnabled) {
        error(body.invoke(), t)
    }
}

inline fun Logger.trace(t: Throwable? = null, predicate: Boolean = true, body: () -> String) = t(t, predicate, body)

inline fun Logger.debug(t: Throwable? = null, predicate: Boolean = true, body: () -> String) = d(t, predicate, body)

inline fun Logger.info(t: Throwable? = null, predicate: Boolean = true, body: () -> String) = i(t, predicate, body)

inline fun Logger.warn(t: Throwable? = null, predicate: Boolean = true, body: () -> String) = w(t, predicate, body)

inline fun Logger.error(t: Throwable? = null, predicate: Boolean = true, body: () -> String) = e(t, predicate, body)