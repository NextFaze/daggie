package com.nextfaze.daggie.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.filter.ThresholdFilter

fun Level.toThresholdFilter() = ThresholdFilter().apply {
    setLevel(this@toThresholdFilter.toString())
    start()
}
