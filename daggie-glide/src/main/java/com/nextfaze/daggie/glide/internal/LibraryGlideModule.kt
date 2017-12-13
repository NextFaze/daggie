package com.nextfaze.daggie.glide.internal

import android.content.Context
import android.support.annotation.Keep
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.nextfaze.daggie.glide.configureRegistry

@Keep
@RestrictTo(LIBRARY)
@GlideModule
class LibraryGlideModule : com.bumptech.glide.module.LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        configureRegistry!!(registry)
    }
}
