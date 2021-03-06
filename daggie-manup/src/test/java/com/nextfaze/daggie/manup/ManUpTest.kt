@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.manup

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.nextfaze.daggie.Foreground
import com.nextfaze.daggie.Initializer
import dagger.BindsInstance
import dagger.Component
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Singleton

private const val CACHE_MAX_BYTES = 10L * 1024 * 1024
private const val PATH = "/manup"

@Config(manifest = Config.NONE, sdk = [23])
@RunWith(RobolectricTestRunner::class)
class ManUpTest {

    @Rule @JvmField
    val wireMock = WireMockRule(wireMockConfig().dynamicPort())

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var scheduler: TestScheduler
    private lateinit var cache: Cache
    private lateinit var okHttpClient: OkHttpClient

    @Before fun setUp() {
        cache = Cache(File(application.cacheDir, "manUpTestOkHttpCache"), CACHE_MAX_BYTES)
        okHttpClient = OkHttpClient.Builder().cache(cache).build()
        scheduler = TestScheduler()
        RxJavaPlugins.setIoSchedulerHandler { scheduler }
        RxJavaPlugins.setComputationSchedulerHandler { scheduler }
    }

    @After fun tearDown() {
        cache.close()
        cache.directory().delete()
        RxJavaPlugins.reset()
    }

    @Test(timeout = 10000)
    fun `user agent re-validates every request`() {
        wireMock.stubFor(get(PATH).willReturn(manUpResponse1()))
        wireMock.stubFor(get(PATH).willReturn(manUpResponse2()))
        startManUp(application, okHttpClient, manUpConfig(1), foreground(true))
        scheduler.advanceTimeBy(1, SECONDS)
        wireMock.verify(2, getRequestedFor(urlEqualTo(PATH)))
    }

    private fun manUpConfig(pollingIntervalSeconds: Long) = ManUpConfig(
            url = HttpUrl.parse(wireMock.url(PATH))!!,
            pollingInterval = pollingIntervalSeconds,
            pollingIntervalUnit = SECONDS
    )
}

private fun manUpResponse1() = aResponse()
        .withBody("""{
                "manUpAppMaintenanceMode": false,
                "manUpAppVersionCurrent": 1,
                "manUpAppVersionMin": 1,
                "manUpAppUpdateURLMin": "http://example.com/app"
            }""")
        .withHeader("Last-Modified", "Mon, 01 Jan 2000 00:00:00 GMT")

private fun manUpResponse2() = aResponse()
        .withBody("""{
                "manUpAppMaintenanceMode": false,
                "manUpAppVersionCurrent": 2,
                "manUpAppVersionMin": 1,
                "manUpAppUpdateURLMin": "http://example.com/app"
            }""")
        .withHeader("Last-Modified", "Mon, 02 Jan 2000 00:00:00 GMT")

private fun foreground(vararg values: Boolean): Observable<Boolean> =
        Observable.create { emitter -> values.forEach { emitter.onNext(it) } }

@Singleton
@Component(modules = [ManUpModule::class])
interface ManUpTestComponent {
    fun initializers(): @JvmSuppressWildcards Set<Initializer<Application>>

    @Component.Builder
    interface Builder {
        @BindsInstance fun config(manUpConfig: ManUpConfig): Builder
        @BindsInstance fun okHttpClient(okHttpClient: OkHttpClient): Builder
        @BindsInstance fun foreground(@Foreground foreground: Observable<Boolean>): Builder
        fun build(): ManUpTestComponent
    }
}

private fun startManUp(
        application: Application,
        okHttpClient: OkHttpClient,
        config: ManUpConfig,
        foreground: Observable<Boolean>
) = DaggerManUpTestComponent.builder()
        .config(config)
        .okHttpClient(okHttpClient)
        .foreground(foreground)
        .build()
        .initializers()
        .forEach { it(application) }
