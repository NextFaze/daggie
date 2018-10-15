package com.nextfaze.daggie.moshi

import android.net.Uri
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Ordered
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/**
 * Installs Moshi adapters for the following common Android types:
 * * [Uri]
 */
@Module
class AndroidAdaptersModule {
    @Provides @IntoSet
    internal fun configurator() = Ordered<Configurator<Moshi.Builder>>(0) { add(UriAdapter) }
}

private object UriAdapter {
    @ToJson fun toJson(uri: Uri): String = uri.toString()
    @FromJson fun fromJson(value: String): Uri = Uri.parse(value)
}
