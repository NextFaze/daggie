package com.nextfaze.daggie.glide.internal

import android.content.Context
import android.support.annotation.Keep
import android.support.annotation.RestrictTo
import android.support.annotation.RestrictTo.Scope.LIBRARY
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.module.GlideModule
import com.nextfaze.daggie.glide.configureGlide
import com.nextfaze.daggie.glide.configureGlideBuilder

/** Configures Glide via the AndroidManifest, as per standard Glide setup. */
@Keep @RestrictTo(LIBRARY)
class GlideManifestModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) = configureGlideBuilder!!(builder)

    override fun registerComponents(context: Context, glide: Glide) = configureGlide!!(glide)
}
