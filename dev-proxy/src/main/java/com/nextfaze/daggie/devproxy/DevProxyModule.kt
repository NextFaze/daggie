package com.nextfaze.daggie.devproxy

import android.app.Application
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Early
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** Provides bindings that install the dev proxy. */
@Module class DevProxyModule {
    @Provides @Singleton
    internal fun devProxy(config: DevProxyConfig) = DevProxy(config.host, config.port)

    @Provides @IntoSet @Singleton @Early
    internal fun initializer(devProxy: DevProxy): Initializer<Application> = { devProxy.install(it) }

    @Provides @Singleton
    internal fun proxy(devProxy: DevProxy) = devProxy.asProxy()

    @Provides @IntoSet
    internal fun okHttpClientBuilderConfigurator(devProxy: DevProxy): Configurator<OkHttpClient.Builder> = {
        proxy(devProxy.asProxy())
    }
}
