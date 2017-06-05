package com.nextfaze.daggie.stetho

import android.app.Application
import com.facebook.stetho.Stetho
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module class StethoModule {
    @Provides @IntoSet internal fun initializer(): Initializer<Application> = {
        Stetho.initializeWithDefaults(it)
    }
}