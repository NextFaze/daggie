package com.nextfaze.daggie.glide

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.AttrRes
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.CENTER_INSIDE
import android.widget.ImageView.ScaleType.FIT_CENTER
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlin.properties.Delegates.observable

private const val KEY_SUPER_STATE = "superState"
private const val KEY_URI = "uri"

/** A Glide-backed image view that can load a URL. */
@Suppress("MemberVisibilityCanPrivate")
class RemoteImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    /** Drawable resource to display while the image resource is loading. */
    var placeholderResource: Int by invalidateGlideIfChanged(0)

    /** Drawable resource to display if an image load fails. */
    var errorResource: Int by invalidateGlideIfChanged(0)

    /**
     * Drawable resource to display if [uri] is `null`.
     * If a fallback is not set, `null` URLs will cause [errorResource] to be displayed. If the error drawable is not
     * set, [placeholderResource] will be displayed.
     */
    var fallbackResource: Int by invalidateGlideIfChanged(0)

    /** The `Bitmap` `Transformation`s to be applied in order. */
    var transformations: List<Transformation<Bitmap>> by invalidateGlideIfChanged(emptyList(), clear = true)

    /** The URL of the image to be loaded. */
    var uri: Uri? by invalidateGlideIfChanged(null, clear = true)

    /** Controls if a cross fade transition is applied or not. `true` by default. */
    var crossFadeEnabled: Boolean = true

    /** The duration in milliseconds of the cross fade transition. */
    var crossFadeDuration: Long = 300L

    private var imageWidth = 0

    private var imageHeight = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.daggie_glide_RemoteImageView, defStyleAttr, 0).apply {
            try {
                placeholderResource =
                        getResourceId(R.styleable.daggie_glide_RemoteImageView_daggie_glide_placeholderDrawable,
                                placeholderResource)
                errorResource =
                        getResourceId(R.styleable.daggie_glide_RemoteImageView_daggie_glide_errorDrawable,
                                errorResource)
                fallbackResource =
                        getResourceId(R.styleable.daggie_glide_RemoteImageView_daggie_glide_fallbackDrawable,
                                fallbackResource)
                crossFadeEnabled =
                        getBoolean(R.styleable.daggie_glide_RemoteImageView_daggie_glide_crossFadeEnabled,
                                crossFadeEnabled)
                crossFadeDuration =
                        getInteger(R.styleable.daggie_glide_RemoteImageView_daggie_glide_crossFadeDuration,
                                crossFadeDuration.toInt()).toLong()
            } finally {
                recycle()
            }
        }
        updateDimensions()
    }

    override fun onSaveInstanceState() = Bundle().apply {
        putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        putParcelable(KEY_URI, uri)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
            uri = state.getParcelable(KEY_URI)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDimensions()
    }

    private fun updateDimensions() {
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        if (width != imageWidth || height != imageHeight) {
            imageWidth = width
            imageHeight = height
            invalidateGlide(clear = true)
        }
    }

    private fun invalidateGlide(clear: Boolean = false) {
        if (clear) Glide.with(context).clear(this)
        load()
    }

    private fun load() {
        if (!isInEditMode && imageWidth > 0 && imageHeight > 0) {
            Glide.with(context).load(uri).apply(requestOptions()).transition(transitionOptions()).into(this)
        }
    }

    private fun requestOptions(): RequestOptions {
        var requestOptions = RequestOptions()
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (scaleType) {
            CENTER_CROP -> requestOptions = requestOptions.centerCrop()
            FIT_CENTER, CENTER_INSIDE -> requestOptions = requestOptions.fitCenter()
        }
        if (placeholderResource > 0) requestOptions = requestOptions.placeholder(placeholderResource)
        if (errorResource > 0) requestOptions = requestOptions.error(errorResource)
        if (fallbackResource > 0) requestOptions = requestOptions.fallback(fallbackResource)
        if (!transformations.isEmpty()) requestOptions = requestOptions.transforms(*transformations.toTypedArray())
        return requestOptions.override(imageWidth, imageHeight)
    }

    private fun transitionOptions(): DrawableTransitionOptions = if (crossFadeEnabled) {
        DrawableTransitionOptions.withCrossFade(crossFadeDuration.toInt())
    } else {
        DrawableTransitionOptions().dontTransition()
    }

    private fun <T> invalidateGlideIfChanged(initialValue: T, clear: Boolean = false) =
            observable(initialValue) { _, old, new -> if (old != new) invalidateGlide(clear) }
}
