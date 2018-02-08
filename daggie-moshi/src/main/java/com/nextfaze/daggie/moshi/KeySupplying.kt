package com.nextfaze.daggie.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.BEGIN_OBJECT
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.collectionElementType
import com.squareup.moshi.Types.getRawType
import com.squareup.moshi.Types.newParameterizedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Provides `JsonAdapter`s that supply a special key `_key` to downstream adapters. This string can be used with
 * [Json.name] to embed the property name at which the JSON object resides in a parent object.
 * @see RequiresKey
 */
class KeySupplyingJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        // The only types we handle are parameterized
        if (type !is ParameterizedType) return null

        val rawType = getRawType(type)

        // Adapter for maps of plain java objects
        val valueMapAdapter = moshi.nextAdapter<Map<*, *>>(this, MAP_OF_ANY_TO_ANY, annotations)

        when {
            Collection::class.java.isAssignableFrom(rawType) -> {
                val elementType = collectionElementType(type, rawType)
                if (!elementType.isEligibleForKeyInsertion) return null

                // Reads a collection of the desired element type
                val collectionType = newParameterizedType(rawType, elementType)
                val collectionAdapter = moshi.nextAdapter<Collection<*>>(this, collectionType, annotations)

                return object : JsonAdapter<Collection<*>>() {
                    override fun fromJson(reader: JsonReader): Collection<*>? {
                        reader.expectObjectToRead(elementType)
                        // Read as a java map
                        val mapOfMapsWithoutKeys = valueMapAdapter.fromJson(reader)
                        // Add keys
                        val listOfMapsWithKeys =
                            mapOfMapsWithoutKeys?.let { map -> map.entries.map { it.addKey(reader.path) } }
                        // Parse list of maps
                        return collectionAdapter.fromJsonValue(listOfMapsWithKeys)
                    }

                    override fun toJson(writer: JsonWriter, collection: Collection<*>?) {
                        // Write collection as list of maps.
                        // Safe to assume elements are all maps, since we only support parsing models annotated
                        // with our annotation, so therefore they are expected to all be represented as JSON
                        // objects.
                        @Suppress("UNCHECKED_CAST")
                        val listOfMapsWithKeys =
                            collection?.let { collectionAdapter.toJsonValue(collection) as List<Map<*, *>> }
                        // Convert to map
                        val mapOfMapsWithKeys = listOfMapsWithKeys?.associateBy { it[KEY] }
                        // Remove keys
                        val mapOfMapsWithoutKeys = mapOfMapsWithKeys?.mapValues { it.value.withoutKey() }
                        // Write map of maps
                        valueMapAdapter.toJson(writer, mapOfMapsWithoutKeys)
                    }
                }.nullSafe()
            }
            Map::class.java.isAssignableFrom(rawType) -> {
                val valueType = type.actualTypeArguments[1]
                if (!valueType.isEligibleForKeyInsertion) return null

                // Reads a map of the desired element type
                val mapType = newParameterizedType(rawType, Any::class.java, valueType)
                val mapAdapter = moshi.nextAdapter<Map<*, *>>(this, mapType, annotations)

                return object : JsonAdapter<Map<*, *>>() {
                    override fun fromJson(reader: JsonReader): Map<*, *>? {
                        reader.expectObjectToRead(valueType)
                        // Read as java map
                        val mapOfMapsWithoutKeys = valueMapAdapter.fromJson(reader)
                        // Add keys
                        val mapOfMapsWithKeys = mapOfMapsWithoutKeys?.mapValues { it.addKey(reader.path) }
                        // Parse map of maps
                        return mapAdapter.fromJsonValue(mapOfMapsWithKeys)
                    }

                    override fun toJson(writer: JsonWriter, map: Map<*, *>?) {
                        // Write map as map of maps
                        @Suppress("UNCHECKED_CAST")
                        val mapOfMapsWithKeys =
                            map?.let { mapAdapter.toJsonValue(map) as Map<*, Map<*, *>> }
                        // Remove keys
                        val mapOfMapsWithoutKeys = mapOfMapsWithKeys?.mapValues { it.value.withoutKey() }
                        // Write map of maps
                        valueMapAdapter.toJson(writer, mapOfMapsWithoutKeys)
                    }
                }.nullSafe()
            }
            else -> return null
        }
    }

    /** Adds [KEY], which points to the [Map.Entry.key], to this entry's map. */
    private fun Map.Entry<Any?, Any?>.addKey(readerPath: String): Any? {
        @Suppress("UNCHECKED_CAST")
        val entryValue = value
        return when (entryValue) {
            is Map<*, *> -> {
                // Copy source map from entry value, then add the entry's key into it as a special key
                entryValue.toMutableMap().apply {
                    if (put(KEY, key) != null) {
                        throw JsonDataException("$KEY already exists and would be overridden at $readerPath")
                    }
                }

            }
            else -> entryValue
        }
    }

    /** Returns a copy of this map without the [KEY] entry. */
    private fun Map<*, *>.withoutKey(): Map<*, *> = toMutableMap().apply { remove(KEY) }

    companion object {
        /** Defines the special key we insert into maps based its key in the parent map. */
        @PublishedApi internal const val KEY = "_key"
    }
}

private fun JsonReader.expectObjectToRead(elementType: Type) {
    if (peek() != BEGIN_OBJECT) {
        throw JsonDataException("$elementType declared ${RequiresKey::class.java.name}, so expected BEGIN_OBJECT at $path, but got ${peek()}")
    }
}

/** Indicates if the type is eligible to have its map entry key made available for parsing. */
private val Type.isEligibleForKeyInsertion
    get() = this is Class<*> && !this.isPrimitive && !this.isArray && !this.isEnum &&
            !this.isInterface && !this.name.startsWith("java.") && !this.name.startsWith("android.") &&
            this.isAnnotationPresent(RequiresKey::class.java)

/**
 * If present on a class, [KeySupplyingJsonAdapterFactory] propagates the key at which instances of the class reside.
 * Members of the class can access the key using the `_key` special string.
 *
 * Usage of this annotation means a [Collection] of objects will be written not as a JSON array, but an object, in order
 * to maintain serialization compatibility in both directions.
 *
 * Example usage:
 *
 * ```
 * val moshi = Moshi.Builder()
 *     // Kotlin adapter factory must come first
 *     .add(KotlinJsonAdapterFactory())
 *     .add(KeySupplyingJsonAdapterFactory())
 *     .build()
 *
 * @RequiresKey
 * data class Item(@Json(name = "_key") val id: String, val name: String)
 *
 * val adapter = moshi.adapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))
 * val json = """{
 *     "1": { "name": "Foo" },
 *     "2": { "name": "Bar" }
 * }"""
 * val items = adapter.fromJson(json)
 *
 * Assert.assertEquals(listOf(Item("1", "Foo"), Item("2", "Bar")), items)
 * ```
 *
 * @see KeySupplyingJsonAdapterFactory
 */
@Retention(RUNTIME)
@Target(CLASS)
annotation class RequiresKey

private val MAP_OF_ANY_TO_ANY = newParameterizedType(Map::class.java, Any::class.java, Any::class.java)
