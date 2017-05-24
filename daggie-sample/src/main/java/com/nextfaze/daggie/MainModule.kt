package com.nextfaze.daggie

import dagger.Module

/** Provides all main app bindings that have no reasonable alternatives. */
@Module(includes = arrayOf(
        AndroidModule::class,
        InitializerModule::class
//        RxJavaModule::class,
//        ThreeTenModule::class,
//        ClockModule::class,
//        PrettyTimeModule::class,
//        GlideModule::class,
//        OkHttpModule::class,
//        ForegroundModule::class,
//        LogbackModule::class,
//        ErrorModule::class,
//        NavigationModule::class,
//        ContentModule::class,
//        MapModule::class,
//        AboutModule::class,
//        RecentsModule::class,
//        SocialModule::class,
//        JSoupModule::class,
//        BackgroundModule::class,
//        BootModule::class,
//        ManUpModule::class
))
class MainModule {
    // Bindings that might go here: user agent, ManUp config URL
}