package com.nextfaze.daggie.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder

/**
 * A app Glide module that configures [GlideBuilder] with the `Configurator<GlideBuilder>` set bindings. This class
 * does nothing until subclassed and annotated with [com.bumptech.glide.annotation.GlideModule] in an application.
 * Apps should extend this when implementing their own [com.bumptech.glide.module.AppGlideModule]s.
 */
open class AppGlideModule : com.bumptech.glide.module.AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        configureGlideBuilder!!(builder)
    }
}
