package com.nextfaze.daggie.okhttp

import android.content.Context
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

private const val MAX_CACHE_SIZE_BYTES: Long = 10 * 1024 * 1024

/**
 * Provides an [OkHttpClient] binding, which is configured with a cache.
 *
 * Users can further configure the client by providing:
 * * `Configurator<OkHttpClient.Builder>` set bindings to append additional configuration to the [OkHttpClient.Builder]
 * * `Ordered<Interceptor>` set bindings to add [interceptors][OkHttpClient.Builder.addInterceptor] in the order defined
 * by [Ordered.order]
 * * `Interceptor` set bindings to add interceptors in an undefined order AFTER the ordered interceptors
 * * `@Network Ordered<Interceptor>` set bindings to add
 * [network interceptors][OkHttpClient.Builder.addNetworkInterceptor] in the order defined by [Ordered.order]
 * * `@Network Interceptor` set bindings to add network interceptors in an undefined order AFTER the ordered network
 * interceptors
 * @see OkHttpClient.Builder.addInterceptor
 * @see OkHttpClient.Builder.addNetworkInterceptor
 * @see Network
 * @see Ordered
 */
@Module class OkHttpModule {
    @Provides @Singleton
    internal fun okHttpClient(
            cache: Cache,
            configurators: @JvmSuppressWildcards Set<Configurator<OkHttpClient.Builder>>,
            orderedInterceptorEntries: @JvmSuppressWildcards Set<Ordered<Interceptor>>,
            unorderedInterceptorEntries: @JvmSuppressWildcards Set<Interceptor>,
            @Network orderedNetworkInterceptorEntries: @JvmSuppressWildcards Set<Ordered<Interceptor>>,
            @Network unorderedNetworkInterceptorEntries: @JvmSuppressWildcards Set<Interceptor>
    ): OkHttpClient {
        val builder = OkHttpClient.Builder().cache(cache)
        configurators.forEach { it(builder) }
        orderedNetworkInterceptorEntries.asSequence().sorted().map { it.value }.forEach { builder.addNetworkInterceptor(it) }
        unorderedNetworkInterceptorEntries.forEach { builder.addNetworkInterceptor(it) }
        orderedInterceptorEntries.asSequence().sorted().map { it.value }.forEach { builder.addInterceptor(it) }
        unorderedInterceptorEntries.forEach { builder.addInterceptor(it) }
        return builder.build()
    }

    // TODO: Allow external Cache binding using Optional<Cache>

    @Provides @Singleton
    internal fun cache(context: Context) = Cache(File(context.cacheDir, "okhttp-cache"), MAX_CACHE_SIZE_BYTES)

    @Provides @ElementsIntoSet
    internal fun defaultOkHttpClientBuilderConfigurators() = emptySet<Configurator<OkHttpClient.Builder>>()

    @Provides @ElementsIntoSet
    internal fun defaultOrderedInterceptors() = emptySet<@JvmSuppressWildcards Ordered<Interceptor>>()

    @Provides @ElementsIntoSet
    internal fun defaultUnorderedInterceptors() = emptySet<@JvmSuppressWildcards Interceptor>()

    @Provides @ElementsIntoSet @Network
    internal fun defaultOrderedNetworkInterceptors() = emptySet<@JvmSuppressWildcards Ordered<Interceptor>>()

    @Provides @ElementsIntoSet @Network
    internal fun defaultUnorderedNetworkInterceptors() = emptySet<@JvmSuppressWildcards Interceptor>()
}