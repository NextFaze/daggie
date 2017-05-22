package com.nextfaze.daggie

/** A function that is invoked on the target in order to configure it. */
typealias Configurator<T> = T.() -> Unit