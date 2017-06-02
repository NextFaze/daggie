package com.nextfaze.daggie.jodatime

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import net.danlew.android.joda.JodaTimeAndroid

/** Provides bindings that initialize the JodaTime Android library. */
@Module class JodaTimeModule {
    @Provides @IntoSet fun initializer() = Ordered<Initializer<Application>>(0) { JodaTimeAndroid.init(it) }
}