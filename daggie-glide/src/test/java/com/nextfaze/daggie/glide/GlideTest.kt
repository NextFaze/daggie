@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.glide

import android.app.Application
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.load.ImageHeaderParser
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.glide.internal.LibraryGlideModule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import javax.inject.Singleton
import kotlin.concurrent.thread

@Config(manifest = Config.NONE, sdk = [23])
@RunWith(RobolectricTestRunner::class)
class GlideTest {

    @Rule @JvmField
    val wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort())

    private lateinit var trackingInterceptor: TrackingInterceptor
    private lateinit var okHttpClient: OkHttpClient
    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Before fun setUp() {
        trackingInterceptor = TrackingInterceptor()
        okHttpClient = OkHttpClient.Builder().addInterceptor(trackingInterceptor).build()
    }

    @Test(timeout = 10000)
    fun `bound okhttpclient is used to execute image load`() {
        ShadowLooper.idleMainLooperConstantly(true)
        wireMock.stubFor(get("/img").willReturn(imageResponse()))
        initGlide(okHttpClient, glideBuilderConfigurators = setOf<Configurator<GlideBuilder>> {
            setLogLevel(Log.VERBOSE)
        })
        val future = Glide.with(application).asBitmap().load(wireMock.url("/img")).submit()
        // Spin until Glide's worker thread eventually posts a callback with the image result
        while (!Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable()) {
        }
        thread { future.get() }.join()
        wireMock.verify(1, getRequestedFor(urlEqualTo("/img")))
        trackingInterceptor.assertContainsUrlWithSuffix("/img")
    }

    @Test fun `library glide module executes successfully`() {
        initGlide(okHttpClient)
        LibraryGlideModule().registerComponents(mock(), mock(), mock())
    }

    @Test fun `glide builder configurator is executed`() {
        initGlide(okHttpClient, glideBuilderConfigurators = setOf<Configurator<GlideBuilder>> {
            setLogLevel(Log.VERBOSE)
        })
        val glideBuilder = mock<GlideBuilder>()
        AppGlideModule().applyOptions(mock(), glideBuilder)
        verify(glideBuilder).setLogLevel(Log.VERBOSE)
    }

    @Test fun `registry configurator is executed`() {
        val imageHeaderParser = mock<ImageHeaderParser>()
        initGlide(okHttpClient, registryConfigurators = setOf<Configurator<Registry>> {
            register(imageHeaderParser)
        })
        val registry = mock<Registry>()
        LibraryGlideModule().registerComponents(mock(), mock(), registry)
        verify(registry).register(imageHeaderParser)
    }
}

@com.bumptech.glide.annotation.GlideModule
@Excludes(OkHttpLibraryGlideModule::class)
class TestAppGlideModule : AppGlideModule()

@Singleton
@Component(modules = [GlideModule::class, TestModule::class])
interface GlideComponent {
    fun initializers(): @JvmSuppressWildcards Set<Initializer<Application>>

    @Component.Builder
    interface Builder {
        @BindsInstance fun okHttpClient(okHttpClient: OkHttpClient): Builder
        fun testModule(testModule: TestModule): Builder
        fun build(): GlideComponent
    }
}

@Module data class TestModule(
        @get:Provides
        @get:ElementsIntoSet
        val glideBuilderConfigurators: @JvmSuppressWildcards Set<Configurator<GlideBuilder>>,

        @get:Provides
        @get:ElementsIntoSet
        val registryConfigurators: @JvmSuppressWildcards Set<Configurator<Registry>>
)

private fun initGlide(
        okHttpClient: OkHttpClient,
        glideBuilderConfigurators: @JvmSuppressWildcards Set<Configurator<GlideBuilder>> = emptySet(),
        registryConfigurators: @JvmSuppressWildcards Set<Configurator<Registry>> = emptySet()
) {
    val component = DaggerGlideComponent.builder()
            .okHttpClient(okHttpClient)
            .testModule(TestModule(glideBuilderConfigurators, registryConfigurators))
            .build()
    val application = mock<Application>()
    component.initializers().forEach { it(application) }
}

private const val MINIMAL_VALID_PNG_BASE_64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAACklEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=="

private fun imageResponse() = aResponse()
        .withBase64Body(MINIMAL_VALID_PNG_BASE_64)
