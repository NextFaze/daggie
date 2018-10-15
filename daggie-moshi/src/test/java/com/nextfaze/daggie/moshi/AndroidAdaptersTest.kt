package com.nextfaze.daggie.moshi

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Singleton

@Config(application = RobolectricApplication::class)
@RunWith(RobolectricTestRunner::class)
class AndroidAdaptersTest {

    private val moshi = Component.create().moshi()
    private lateinit var uriAdapter: JsonAdapter<Uri>

    @Before fun setUp() {
        uriAdapter = moshi.adapter(Uri::class.java)
    }

    @Test fun `uri should parse`() {
        val url = "http://example.com/foo"
        @Language("JSON")
        val json = "\"$url\""
        assertThat(uriAdapter.fromJson(json)).isEqualTo(Uri.parse(url))
    }

    @Singleton
    @dagger.Component(modules = [MoshiModule::class, AndroidAdaptersModule::class])
    interface Component {
        fun moshi(): Moshi

        companion object {
            fun create(): Component = DaggerAndroidAdaptersTest_Component.create()
        }
    }
}
