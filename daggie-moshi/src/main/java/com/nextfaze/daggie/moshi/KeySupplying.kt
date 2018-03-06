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
        val rawType = getRawType(type)

        // Adapters used to convert to and from Java value objects.
        // Required because we need to preprocess supported model classes in Map form.
        fun nextValueMapAdapter() =
            moshi.nextAdapter<Map<String, *>>(this, MAP_OF_STRING_TO_ANY, annotations)!!

        fun nextValueMapOfMapsAdapter() =
            moshi.nextAdapter<Map<String, Map<String, *>>>(this, MAP_OF_STRING_TO_MAP_OF_STRING_TO_ANY, annotations)!!

        when {
            Collection::class.java.isAssignableFrom(rawType) -> {
                val elementType = collectionElementType(type, rawType)
                if (!elementType.isEligibleForKeyInsertion) return null

                // Adapter for reading a collection of the desired element type.
                val collectionType = newParameterizedType(rawType, elementType)
                val collectionAdapter = moshi.nextAdapter<Collection<*>>(this, collectionType, annotations)
                val elementAdapter = moshi.nextAdapter<Any>(this, elementType, annotations)

                val valueMapAdapter = nextValueMapAdapter()
                val valueMapOfMapsAdapter = nextValueMapOfMapsAdapter()

                return object : JsonAdapter<Collection<*>>() {
                    /** Reads a collection, adding keys from the enclosing JSON object. */
                    override fun fromJson(reader: JsonReader): Collection<*>? {
                        reader.expectObjectToRead(elementType)
                        // Read as a java map
                        val mapOfMapsWithoutKeys = valueMapOfMapsAdapter.fromJson(reader)!!
                        // Add keys
                        val listOfMapsWithKeys = mapOfMapsWithoutKeys.entries.map { it.plusKey(reader.path) }
                        // Parse list of maps
                        return collectionAdapter.fromJsonValue(listOfMapsWithKeys)
                    }

                    /** Writes a collection as a map, associating the values with their key. */
                    override fun toJson(writer: JsonWriter, collection: Collection<*>?) {
                        writer.beginObject()
                        for (element in collection!!) {
                            // Safe to assume elements are all maps, since we only support parsing custom classes
                            // annotated with our annotation, so therefore they are expected to all be represented as
                            // JSON objects.
                            @Suppress("UNCHECKED_CAST")
                            val mapWithKey = elementAdapter.toJsonValue(element) as Map<String, *>
                            // Key string always expected to be present.
                            // If absent, it either indicates an error in this factory, or the model class
                            // allows null key values. Neither scenario is recoverable.
                            val name = mapWithKey[KEY] as? String
                                    ?: throw JsonDataException("Expected String value for $KEY property at ${writer.path}")
                            writer.name(name)
                            // Remove key
                            val mapWithoutKey = mapWithKey.minusKey()
                            // Write map
                            valueMapAdapter.toJson(writer, mapWithoutKey)
                        }
                        writer.endObject()
                    }

                    override fun toString() = "$collectionAdapter.keySupplying()"
                }.nullSafe()
            }
            Map::class.java.isAssignableFrom(rawType) && type is ParameterizedType -> {
                val valueType = type.actualTypeArguments[1]
                if (!valueType.isEligibleForKeyInsertion) return null

                // Reads a map of the desired element type
                val mapType = newParameterizedType(rawType, String::class.java, valueType)
                // This is expected to delegate back to our single element adapter, which will remove the keys
                val mapAdapter = moshi.nextAdapter<Map<String, *>>(this, mapType, annotations)

                val valueMapOfMapsAdapter = nextValueMapOfMapsAdapter()

                return object : JsonAdapter<Map<String, *>>() {
                    /** Reads a map, adding keys from the enclosing JSON object. */
                    override fun fromJson(reader: JsonReader): Map<String, *>? {
                        reader.expectObjectToRead(valueType)
                        // Read as java map
                        val mapOfMapsWithoutKeys = valueMapOfMapsAdapter.fromJson(reader)!!
                        // Add keys
                        val mapOfMapsWithKeys = mapOfMapsWithoutKeys.mapValues { it.plusKey(reader.path) }
                        // Parse map of maps
                        return mapAdapter.fromJsonValue(mapOfMapsWithKeys)
                    }

                    /** Writes a map, omitting keys from the values. */
                    override fun toJson(writer: JsonWriter, map: Map<String, *>?) = mapAdapter.toJson(writer, map)

                    override fun toString() = "$mapAdapter.keySupplying()"
                }.nullSafe()
            }
            type.isEligibleForKeyInsertion -> {
                val delegatedAdapter = moshi.nextAdapter<Any>(this, type, annotations)
                val valueMapAdapter = nextValueMapAdapter()
                return object : JsonAdapter<Any?>() {
                    /** Reads an object. Doesn't require special treatment because the key will be available by this time. */
                    override fun fromJson(reader: JsonReader): Any? = delegatedAdapter.fromJson(reader)

                    /** Writes an object as a map, omitting the key. */
                    override fun toJson(writer: JsonWriter, value: Any?) {
                        // Safe to assume single objects are represented as maps, since we only support parsing custom
                        // classes annotated with our annotation, so therefore they are expected to all be represented
                        // as JSON objects.
                        @Suppress("UNCHECKED_CAST")
                        val mapWithKey = delegatedAdapter.toJsonValue(value) as Map<String, *>
                        // Remove key
                        val mapWithoutKey = mapWithKey.minusKey()
                        // Write map
                        valueMapAdapter.toJson(writer, mapWithoutKey)
                    }

                    override fun toString() = "$delegatedAdapter.keySupplying()"
                }.nullSafe()
            }
            else -> return null
        }
    }

    /** Adds [KEY], which points to the [Map.Entry.key], to this entry's map. */
    private fun Map.Entry<String, Map<String, *>>.plusKey(readerPath: String): Map<String, *> =
        value.toMutableMap().apply {
            if (put(KEY, key) != null) throw JsonDataException("$KEY already exists and would be overridden at $readerPath")
        }

    /** Returns a copy of this map without the [KEY] entry. */
    private fun Map<String, *>.minusKey(): Map<String, *> = filterKeys { it != KEY }

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
 *     .add(KeySupplyingJsonAdapterFactory())
 *     // Kotlin adapter factory must come last
 *     .add(KotlinJsonAdapterFactory())
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

private val MAP_OF_STRING_TO_ANY = newParameterizedType(Map::class.java, String::class.java, Any::class.java)
private val MAP_OF_STRING_TO_MAP_OF_STRING_TO_ANY =
    newParameterizedType(Map::class.java, String::class.java, Map::class.java, String::class.java, Any::class.java)
