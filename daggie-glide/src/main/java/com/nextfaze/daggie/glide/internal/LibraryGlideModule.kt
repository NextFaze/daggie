package com.nextfaze.daggie.glide.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.nextfaze.daggie.glide.configureRegistry

@Keep
@RestrictTo(LIBRARY)
@GlideModule
class LibraryGlideModule : com.bumptech.glide.module.LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) = configureRegistry(registry)
}
