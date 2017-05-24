package com.nextfaze.daggie.manup

import android.app.Application
import com.nextfaze.daggie.Foreground
import com.nextfaze.daggie.Initializer
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import rx.Observable

@Module class ManUpModule {
    @Provides @IntoSet internal fun initializer(
            httpClient: OkHttpClient,
            @ManUpConfigUrl url: String,
            @Foreground foreground: Observable<Boolean>
    ): Initializer<Application> = { initManUp(it, httpClient, foreground, url) }
}