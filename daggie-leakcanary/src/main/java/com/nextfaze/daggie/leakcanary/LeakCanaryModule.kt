package com.nextfaze.daggie.leakcanary

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.squareup.leakcanary.LeakCanary
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.slf4j.LoggerFactory

@Module class LeakCanaryModule {
    private val log = LoggerFactory.getLogger(LeakCanaryModule::class.java.name)

    @Provides @IntoSet internal fun initializer(): Initializer<Application> = {
        // Don't install LeakCanary when using JRebel
        // It is rarely able to identify the source of the leak due to JRebel (shadow classes etc.) and just gets in the way
        if (!this::class.java.classLoader::class.java.name.contains("jrebel")) {
            LeakCanary.install(it)
            log.debug("LeakCanary installed.")
        } else {
            log.debug("JRebel instance detected - skipping LeakCanary installation.")
        }
    }
}
