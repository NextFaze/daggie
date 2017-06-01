package com.example.app

import com.nextfaze.daggie.devproxy.DevProxyConfig
import com.nextfaze.daggie.devproxy.DevProxyModule
import com.nextfaze.daggie.sample.BuildConfig
import dagger.Module
import dagger.Provides

/** Provides additional debug-only bindings, such as those supplying dev menus or debugging tools. */
@Module(includes = arrayOf(
        DevProxyModule::class
//        StrictModeModule::class,
//        StethoModule::class,
//        LeakCanaryModule::class
))
class DebugModule {
    @Provides internal fun devProxyConfig() = DevProxyConfig(BuildConfig.DEV_PROXY_HOST, BuildConfig.DEV_PROXY_PORT)
}