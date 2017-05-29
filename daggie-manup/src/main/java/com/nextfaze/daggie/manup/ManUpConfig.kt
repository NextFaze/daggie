package com.nextfaze.daggie.manup

import okhttp3.HttpUrl
import java.util.concurrent.TimeUnit

/**
 * Represents configuration of the ManUp system. Not to be confused with the ManUp config itself.
 * Applications should provide a binding for this type, in order to use the ManUp module.
 * @property url The URL of the ManUp configuration JSON resource.
 * @property pollingInterval Defines the amount of time between checks against the remote config at [url].
 * @property pollingIntervalUnit Defines the unit for [pollingInterval].
 */
data class ManUpConfig(
        val url: HttpUrl,
        val pollingInterval: Long = 10L,
        val pollingIntervalUnit: TimeUnit = TimeUnit.MINUTES
)