package com.nextfaze.daggie.manup

import com.google.gson.annotations.SerializedName

/** Container for the platform unified mandatory update configuration. */
class PlatformUnifiedConfig {
    @field:SerializedName("android")
    internal lateinit var config: Config
}
