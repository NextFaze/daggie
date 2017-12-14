package com.nextfaze.daggie.glide

import android.app.Application
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import java.io.InputStream

/** Hack to "inject" private GlideBuilder configurator set. */
@RestrictTo(LIBRARY)
lateinit internal var configureGlideBuilder: ((GlideBuilder) -> Unit)

/** Hack to "inject" private Glide configurator set. */
@RestrictTo(LIBRARY)
lateinit internal var configureRegistry: ((Registry) -> Unit)

/**
 * Provides bindings that initialize Glide. Requires an [OkHttpClient] binding.
 *
 * Users can further configure Glide by providing:
 * * `Configurator<GlideBuilder>` set bindings to append additional configuration to [GlideBuilder]
 * * `Configurator<Registry>` set bindings to append additional configuration to [Registry]
 */
@Module class GlideModule {
    @Provides @IntoSet
    internal fun initializer(
            glideBuilderConfigurators: @JvmSuppressWildcards Set<Configurator<GlideBuilder>>,
            registryConfigurators: @JvmSuppressWildcards Set<Configurator<Registry>>
    ): Initializer<Application> = {
        configureGlideBuilder = { glideBuilder -> glideBuilderConfigurators.forEach { it(glideBuilder) } }
        configureRegistry = { registry -> registryConfigurators.forEach { it(registry) } }
    }

    @Provides @IntoSet
    internal fun registryConfigurator(okHttpClient: OkHttpClient): Configurator<Registry> = {
        // We don't want OkHttp cache as glide does it for us.
        replace(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(okHttpClient.newBuilder().cache(null).build()))
    }

    @Provides @ElementsIntoSet
    internal fun defaultGlideBuilderConfigurators() = emptySet<Configurator<GlideBuilder>>()

    @Provides @ElementsIntoSet
    internal fun defaultRegistryConfigurators() = emptySet<Configurator<Registry>>()
}
