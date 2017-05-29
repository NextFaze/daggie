package com.nextfaze.daggie.logback

import ch.qos.logback.classic.Level

/**
 * Contains configuration for the Logback module.
 * @property rootLevel The [Level] to use for the [root logger][org.slf4j.Logger.ROOT_LOGGER_NAME]. This can be `null`
 * to use a default log level based on the [debuggable][android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE] status of
 * the app.
 * @property configAssetPath The asset path of the Logback configuration XML file. May be `null` to disable loading any
 * config from assets.
 * @see ch.qos.logback.classic.Logger.setLevel
 */
data class LogbackConfig(val rootLevel: Level? = null, val configAssetPath: String? = "logback.xml")