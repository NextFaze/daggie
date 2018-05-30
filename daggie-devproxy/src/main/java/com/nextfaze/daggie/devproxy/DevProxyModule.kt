package com.nextfaze.daggie.devproxy

import android.app.Application
import android.security.KeyChain
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Singleton

/**
 * Provides bindings that install the dev proxy into:
 * * The global [OkHttpClient] binding via the [OkHttpClient.Builder]
 * * The system via [KeyChain.createInstallIntent]
 */
@Module class DevProxyModule {
    @Provides @Singleton
    internal fun devProxy(config: DevProxyConfig) = DevProxy(config.host, config.port)

    @Provides @IntoSet @Singleton
    internal fun initializer(devProxy: DevProxy) = Ordered<Initializer<Application>>(0) { devProxy.install(it) }

    @Provides @Singleton
    internal fun proxy(devProxy: DevProxy) = devProxy.asProxy()

    @Provides @IntoSet
    internal fun okHttpClientBuilderConfigurator(devProxy: DevProxy): Configurator<OkHttpClient.Builder> = {
        val proxy = devProxy.asProxy()
        if (proxy.hasValidSocketAddress()) {
            proxy(proxy)
        }
    }
}

private fun Proxy.hasValidSocketAddress(): Boolean {
    val address = (address() as InetSocketAddress?)
    return address != null && !address.hostName.isNullOrEmpty() && address.port > 0
}
