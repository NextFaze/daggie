package com.example.app

import com.nextfaze.daggie.AndroidModule
import com.nextfaze.daggie.autodispose.AutoDisposeModule
import com.nextfaze.daggie.foreground.ForegroundModule
import com.nextfaze.daggie.glide.GlideModule
import com.nextfaze.daggie.gson.GsonModule
import com.nextfaze.daggie.jodatime.JodaTimeModule
import com.nextfaze.daggie.logback.LogbackConfig
import com.nextfaze.daggie.logback.LogbackModule
import com.nextfaze.daggie.manup.ManUpConfig
import com.nextfaze.daggie.moshi.MoshiModule
import com.nextfaze.daggie.okhttp.OkHttpModule
import com.nextfaze.daggie.permissions.PermissionsModule
import com.nextfaze.daggie.rxjava2.RxJava2ErrorHooksModule
import com.nextfaze.daggie.slf4j.*
import com.nextfaze.daggie.threeten.ThreeTenModule
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl

/** Provides all main app bindings that have no reasonable alternatives. */
@Module(includes = arrayOf(
        AndroidModule::class,
        Slf4jModule::class,
        LogbackModule::class,
        RxJava2ErrorHooksModule::class,
//        ManUpModule::class,
        ForegroundModule::class,
        OkHttpModule::class,
        ThreeTenModule::class,
        JodaTimeModule::class,
        GlideModule::class,
        GsonModule::class,
        MoshiModule::class,
        PermissionsModule::class,
        AutoDisposeModule::class
))
class MainModule {
    @Provides internal fun logbackConfig() = LogbackConfig()

    @Provides internal fun manUpConfig() = ManUpConfig(HttpUrl.parse("http://beac.onl/wpa/manup-android")!!)
}
