package com.nextfaze.daggie.glide

import android.app.Application
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
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
internal var configureGlideBuilder: ((GlideBuilder) -> Unit)? = null

/** Hack to "inject" private Glide configurator set. */
@RestrictTo(LIBRARY)
internal var configureGlide: ((Glide) -> Unit)? = null

/**
 * Provides bindings that initialize Glide. Requires an [OkHttpClient] binding.
 *
 * Users can further configure Glide by providing:
 * * `Configurator<GlideBuilder>` set bindings to append additional configuration to [GlideBuilder]
 * * `Configurator<Glide>` set bindings to append additional configuration to the [Glide]
 */
@Module class GlideModule {
    @Provides @IntoSet internal fun initializer(
            glideBuilderConfigurators: Set<@JvmSuppressWildcards Configurator<GlideBuilder>>,
            glideConfigurators: Set<@JvmSuppressWildcards Configurator<Glide>>
    ): Initializer<Application> = {
        configureGlideBuilder = { glideBuilder -> glideBuilderConfigurators.forEach { it(glideBuilder) } }
        configureGlide = { glide -> glideConfigurators.forEach { it(glide) } }
    }

    @Provides @IntoSet internal fun glideConfigurator(okHttpClient: OkHttpClient): Configurator<Glide> = {
        // We don't want OkHttp cache as glide does it for us.
        register(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(okHttpClient.newBuilder().cache(null).build()))
    }

    @Provides @ElementsIntoSet internal fun defaultGlideBuilderConfigurators() = emptySet<Configurator<GlideBuilder>>()

    @Provides @ElementsIntoSet internal fun defaultGlideConfigurators() = emptySet<Configurator<Glide>>()
}