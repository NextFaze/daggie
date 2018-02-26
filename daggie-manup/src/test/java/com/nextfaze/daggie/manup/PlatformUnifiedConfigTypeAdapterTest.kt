package com.nextfaze.daggie.manup

import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

private const val LEGACY_JSON = """{
  "manUpAppMaintenanceMode": false,
  "manUpAppVersionCurrent": 1,
  "manUpAppVersionMin": 1,
  "manUpAppUpdateURLMin": "http://example.com/"
}"""

private const val UNIFIED_JSON = """{
  "android": {
    "manUpAppMaintenanceMode": false,
    "manUpAppVersionCurrent": 1,
    "manUpAppVersionMin": 1,
    "manUpAppUpdateURLMin": "http://example.com/"
  }
}"""

private val TEST_CONFIG = Config(false, 1, 1, "http://example.com/")

class PlatformUnifiedConfigTypeAdapterTest {

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(PlatformUnifiedConfigTypeAdapterFactory())
        .create()!!

    @Test fun readsLegacy() {
        val config = gson.fromJson(LEGACY_JSON, Config::class.java)
        assertThat(config).isEqualTo(TEST_CONFIG)
    }

    @Test fun readsUnified() {
        val config = gson.fromJson(UNIFIED_JSON, Config::class.java)
        assertThat(config).isEqualTo(TEST_CONFIG)
    }

    @Test fun writesLegacy() {
        JSONAssert.assertEquals(gson.toJson(TEST_CONFIG), LEGACY_JSON, false)
    }
}