package com.nextfaze.daggie.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

/** Returns a JSON adapter for type [T], creating it if necessary. */
inline fun <reified T> Moshi.adapter(): JsonAdapter<T> = adapter<T>(T::class.java)
