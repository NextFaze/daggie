package com.nextfaze.daggie.manup

import okhttp3.HttpUrl

/**
 * Represents configuration of the ManUp system. Not to be confused with the ManUp config itself.
 * Applications should provide a binding for this type, in order to use the ManUp module.
 * @property url The URL of the ManUp configuration JSON resource.
 */
data class ManUpConfig(val url: HttpUrl)