package com.nextfaze.daggie.manup

import com.google.gson.TypeAdapterFactory
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory

@GsonTypeAdapterFactory internal abstract class ManUpAutoValueTypeAdapterFactory : TypeAdapterFactory {
    companion object {
        @JvmStatic fun create(): TypeAdapterFactory = AutoValueGson_ManUpAutoValueTypeAdapterFactory()
    }
}