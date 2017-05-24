package com.nextfaze.daggie

import dagger.Module

/** Provides additional debug-only bindings, such as those supplying dev menus or debugging tools. */
@Module(includes = arrayOf(
//        DevProxyModule::class,
//        StrictModeModule::class,
//        StethoModule::class,
//        LeakCanaryModule::class
))
class DebugModule