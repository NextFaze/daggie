package com.nextfaze.daggie.rxjava2

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Singleton

/**
 * Registers RxJava 2 exception-catching [hooks][RxJavaPlugins], ensuring they're all caught and logged.
 * @see RxJavaPlugins
 */
@Module class RxJava2ErrorHooksModule {
    @Provides @IntoSet @Singleton
    internal fun initializer() = Ordered<Initializer<Application>>(0) {
        initErrorHooks()
    }
}
