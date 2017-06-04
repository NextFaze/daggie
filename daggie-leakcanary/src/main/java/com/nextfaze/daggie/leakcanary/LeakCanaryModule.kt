package com.nextfaze.daggie.leakcanary

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.squareup.leakcanary.LeakCanary
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module class LeakCanaryModule {
    @Provides @IntoSet fun initializer(): Initializer<Application> = {
        LeakCanary.install(it)
    }
}