package com.nextfaze.daggie.glide

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.junit.Assert.assertTrue

class TrackingInterceptor : Interceptor {

    private val urls = mutableListOf<HttpUrl>()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) { urls += chain.request().url() }
        return chain.proceed(chain.request())
    }

    fun assertContainsUrlWithSuffix(suffix: String) =
            assertTrue(synchronized(this) { urls.any { it.toString().endsWith(suffix) } })
}
