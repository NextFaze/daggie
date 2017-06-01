package com.example.app

import dagger.Module

/** Provides release build-only bindings. */
@Module(includes = arrayOf(/*FabricModule::class*/))
class ReleaseModule