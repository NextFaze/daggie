package com.nextfaze.daggie.threeten

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/** Provides bindings that initialize the ThreeTen Android Backport library. */
@Module class ThreeTenModule {
    @Provides @IntoSet fun initializer() = Ordered<Initializer<Application>>(0) { AndroidThreeTen.init(it) }
}