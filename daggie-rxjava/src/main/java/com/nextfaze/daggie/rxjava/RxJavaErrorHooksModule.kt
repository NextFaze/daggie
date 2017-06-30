package com.nextfaze.daggie.rxjava

import android.app.Application
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import rx.plugins.RxJavaHooks
import javax.inject.Singleton

/**
 * Registers RxJava exception-catching [hooks][RxJavaHooks], ensuring they're all caught and logged.
 * @see RxJavaHooks
 */
@Module class RxJavaErrorHooksModule {
    @Provides @IntoSet @Singleton
    internal fun initializer() = Ordered<Initializer<Application>>(0) {
        initErrorHooks()
    }
}
