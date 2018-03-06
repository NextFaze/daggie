package com.nextfaze.daggie.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.BEGIN_OBJECT
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.collectionElementType
import com.squareup.moshi.Types.getRawType
import com.squareup.moshi.Types.newParameterizedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Allows parsing JSON objects as [List]s and [Set]s. The input JSON may be an array or object and still
 * be safely parsed. Object keys are discarded when flattening into a collection.
 *
 * Example usage:
 *
 * ```
 * val moshi = Moshi.Builder()
 *     // If using, KeySupplyingJsonAdapterFactory must come first
 *     .add(KeySupplyingJsonAdapterFactory())
 *     .add(MapParsingJsonAdapterFactory())
 *     // Kotlin adapter factory must come last
 *     .add(KotlinJsonAdapterFactory())
 *     .build()
 *
 * val adapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
 * val json = """{
 *     "1": "Foo",
 *     "2": "Bar"
 * }"""
 *
 * val list = adapter.fromJson(json)
 * Assert.assertEquals(listOf("Foo", "Bar"), list)
 * ```
 *
 * @see KeySupplyingJsonAdapterFactory
 */
class MapParsingJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        // The only types we handle are parameterized
        if (type !is ParameterizedType) return null

        val rawType = getRawType(type)

        // We only handle collection types
        if (!Collection::class.java.isAssignableFrom(rawType)) return null

        // Adapter for parsing JSON objects into plain java objects
        val mapAdapter = moshi.nextAdapter<Map<*, *>>(this, MAP_OF_ANY_TO_ANY, annotations)

        // Adapter for reading collection of the desired type
        val elementType = collectionElementType(type, rawType)
        val collectionAdapter =
            moshi.nextAdapter<Collection<*>>(this, newParameterizedType(rawType, elementType), annotations)

        return object : JsonAdapter<Collection<*>>() {
            override fun fromJson(reader: JsonReader): Collection<*>? = if (reader.peek() == BEGIN_OBJECT) {
                // It's a JSON object, so read into map of plain java objects
                val map = mapAdapter.fromJson(reader)
                // Then use collection adapter to read the map's values
                map?.let { collectionAdapter.fromJsonValue(it.values.toList()) }
            } else {
                // Not a JSON object, so read normally as a collection
                collectionAdapter.fromJson(reader)
            }

            override fun toJson(writer: JsonWriter, value: Collection<*>?) {
                // Write collection normally
                collectionAdapter.toJson(writer, value)
            }
        }.nullSafe()
    }
}

private val MAP_OF_ANY_TO_ANY = newParameterizedType(Map::class.java, Any::class.java, Any::class.java)
