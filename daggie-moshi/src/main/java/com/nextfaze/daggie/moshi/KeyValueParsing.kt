package com.nextfaze.daggie.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter

/**
 * Parse the value as [T] using this Moshi [JsonAdapter].
 *
 * If `Moshi` was configured with [KeySupplyingJsonAdapterFactory], the model may access the value of
 * [key] using the special string `_key` in a [Json.name] property declaration:
 *
 * ```
 * data class Foo(
 *     @Json(name = "_key")
 *     val id: String
 * )
 * ```
 *
 * @param key The key `String` corresponding to [value].
 * @param value The value object, comprised of an object tree compatible with [JsonAdapter.fromJsonValue].
 * @param T The type of object to be parsed from the snapshot value.
 * @return An instance of [T], or `null`.
 */
inline fun <reified T : Any> JsonAdapter<T>.fromKeyValuePair(key: String, value: Any?): T? {
    if (value == null) return null
    val isSystemClass = T::class.java.canonicalName.startsWith("java.")
    // Insert key if the value is a map, so it can be parsed into T.
    // Don't if we're parsing a system class like a collection. That causes an error due to mixing the element type.
    val valueWithKey = if (!isSystemClass && value is Map<*, *>) {
        value.toMutableMap().apply { put(KeySupplyingJsonAdapterFactory.KEY, key) }
    } else {
        value
    }
    return fromJsonValue(valueWithKey)
}
