package com.nextfaze.daggie.manup

import android.app.Application
import com.nextfaze.daggie.Foreground
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.Observable
import okhttp3.OkHttpClient

/**
 * Provides bindings that configure "mandatory updates" (aka ManUp). This triggers a dialog that can suggest or
 * force the user to update the app version, based on a remote JSON config file.
 *
 * Users of this module must provide the following bindings:
 * * [OkHttpClient]
 * * [ManUpConfig]
 * * [Foreground] `Observable<Boolean>`
 */
@Module class ManUpModule {
    @Provides @IntoSet internal fun initializer(
            httpClient: OkHttpClient,
            config: ManUpConfig,
            @Foreground foreground: Observable<Boolean>
    ): Initializer<Application> = { initManUp(it, httpClient, foreground, config) }
}
