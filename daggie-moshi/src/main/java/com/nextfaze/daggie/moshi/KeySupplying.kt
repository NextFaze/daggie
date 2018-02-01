package com.nextfaze.daggie.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.collectionElementType
import com.squareup.moshi.Types.getRawType
import com.squareup.moshi.Types.newParameterizedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Provides `JsonAdapter`s that supply a special key `_key` to downstream adapters. This string can be used with
 * [Json.name] to embed the property name at which the JSON object resides in a parent object.
 *
 * Example [Moshi.Builder] configuration:
 *
 * ```
 * val moshi = Moshi.Builder()
 *     // Kotlin adapter factory must come first
 *     .add(KotlinJsonAdapterFactory())
 *     .add(KeySupplyingJsonAdapterFactory())
 *     .build()
 * ```
 */
class KeySupplyingJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        // The only types we handle are parameterized
        if (type !is ParameterizedType) return null

        val rawType = getRawType(type)

        // Adapter for reading a map of plain java objects
        val valueMapType = newParameterizedType(Map::class.java, Any::class.java, Any::class.java)
        val valueMapAdapter = moshi.nextAdapter<Map<*, *>>(this, valueMapType, annotations)

        when {
            Collection::class.java.isAssignableFrom(rawType) -> {
                val elementType = collectionElementType(type, rawType)

                // Reads a collection of the desired element type
                val collectionType = newParameterizedType(rawType, elementType)
                val collectionAdapter = moshi.nextAdapter<Collection<*>>(this, collectionType, annotations)

                return object : JsonAdapter<Collection<*>>() {
                    override fun fromJson(reader: JsonReader): Collection<*>? {
                        // Read as a java map
                        val mapOfMapsWithoutKeys = valueMapAdapter.fromJson(reader)
                        // Add special key entries
                        val listOfMapsWithKeys = mapOfMapsWithoutKeys?.let { map -> map.entries.map { it.addKey() } }
                        // Parse list of maps
                        return collectionAdapter.fromJsonValue(listOfMapsWithKeys)
                    }

                    override fun toJson(writer: JsonWriter, collection: Collection<*>?) {
                        // Read collection as list of maps
                        @Suppress("UNCHECKED_CAST")
                        val listOfMapsWithKeys =
                            collection?.let { collectionAdapter.toJsonValue(collection) as List<Map<*, *>> }
                        // Remove special key entry
                        val mapOfMapsWithoutKeys = listOfMapsWithKeys
                            ?.let { list -> list.associateBy { it[KEY] }.mapValues { it.removeKey() } }
                        // Write map of maps
                        valueMapAdapter.toJson(writer, mapOfMapsWithoutKeys)
                    }
                }
            }
            Map::class.java.isAssignableFrom(rawType) -> {
                val valueType = type.actualTypeArguments[1]

                // Reads a map of the desired element type
                val mapType = newParameterizedType(rawType, Any::class.java, valueType)
                val mapAdapter = moshi.nextAdapter<Map<*, *>>(this, mapType, annotations)

                return object : JsonAdapter<Map<*, *>>() {
                    override fun fromJson(reader: JsonReader): Map<*, *>? {
                        // Read as java map
                        val mapOfMapsWithoutKeys = valueMapAdapter.fromJson(reader)
                        // Add special key entries
                        val mapOfMapsWithKeys = mapOfMapsWithoutKeys?.mapValues { it.addKey() }
                        // Parse map of maps
                        return mapAdapter.fromJsonValue(mapOfMapsWithKeys)
                    }

                    override fun toJson(writer: JsonWriter, map: Map<*, *>?) {
                        // Read map as map of maps
                        @Suppress("UNCHECKED_CAST")
                        val mapOfMapsWithKeys =
                            map?.let { mapAdapter.toJsonValue(map) as Map<*, Map<*, *>> }
                        // Remove special key entry
                        val mapOfMapsWithoutKeys = mapOfMapsWithKeys?.mapValues { it.removeKey() }
                        // Write map of maps
                        valueMapAdapter.toJson(writer, mapOfMapsWithoutKeys)
                    }
                }
            }
            else -> return null
        }
    }

    /** Adds [KEY], which points to the [Map.Entry.key], to this entry's map. */
    private fun Map.Entry<Any?, Any?>.addKey(): Any? {
        @Suppress("UNCHECKED_CAST")
        val entryValue = value
        return when (entryValue) {
            is Map<*, *> -> {
                // Copy source map from entry value, then add the entry's key into it as a special key
                entryValue.toMutableMap().apply {
                    if (put(KEY, key) != null) {
                        throw IllegalArgumentException("${KEY} already exists")
                    }
                }

            }
            else -> entryValue
        }
    }

    /** Strips [KEY] from this entry's map. */
    private fun Map.Entry<*, Map<*, *>>.removeKey(): Map<*, *> = value.toMutableMap().apply { remove(KEY) }

    companion object {
        /** Defines the special key we insert into maps based its key in the parent map. */
        @PublishedApi internal const val KEY = "_key"
    }
}
