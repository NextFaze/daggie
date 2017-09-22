package com.nextfaze.daggie.rxjava2

import com.nextfaze.daggie.slf4j.e
import com.nextfaze.daggie.slf4j.logger
import io.reactivex.plugins.RxJavaPlugins

internal typealias ErrorHandler = (Throwable) -> Unit

private val log = logger("RxJavaErrorHooks")

internal fun initErrorHooks(onError: ErrorHandler = { log.e(it) { "RxJava Error" } }) {
    RxJavaPlugins.setErrorHandler { onError(it) }
}

internal fun clearErrorHooks() {
    RxJavaPlugins.reset()
}
