package com.example.app

import com.nextfaze.daggie.AndroidModule
import com.nextfaze.daggie.foreground.ForegroundModule
import com.nextfaze.daggie.logback.LogbackConfig
import com.nextfaze.daggie.logback.LogbackModule
import com.nextfaze.daggie.manup.ManUpConfig
import com.nextfaze.daggie.manup.ManUpModule
import com.nextfaze.daggie.okhttp.OkHttpModule
import com.nextfaze.daggie.rxjava.RxJavaErrorHooksModule
import com.nextfaze.daggie.slf4j.Slf4jModule
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl

/** Provides all main app bindings that have no reasonable alternatives. */
@Module(includes = arrayOf(
        AndroidModule::class,
        Slf4jModule::class,
        LogbackModule::class,
        RxJavaErrorHooksModule::class,
        ManUpModule::class,
        ForegroundModule::class,
        OkHttpModule::class
))
class MainModule {
    @Provides internal fun logbackConfig() = LogbackConfig()

    @Provides internal fun manUpConfig() = ManUpConfig(HttpUrl.parse("http://beac.onl/wpa/manup-android")!!)
}