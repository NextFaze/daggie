@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.moshi

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Before
import org.junit.Test

class KeySupplyingTest {

    private lateinit var factory: KeySupplyingJsonAdapterFactory

    private lateinit var moshi: Moshi

    private val adapterOfItem get() = moshi.adapter<Item>(Item::class.java)
    private val adapterOfListOfItem
        get() = moshi.adapter<List<Item>>(
            newParameterizedType(List::class.java, Item::class.java)
        )
    private val adapterOfCollectionOfItem
        get() = moshi.adapter<Collection<Item>>(
            newParameterizedType(Collection::class.java, Item::class.java)
        )
    private val adapterOfMapOfItem
        get() = moshi.adapter<Map<String, Item>>(
            newParameterizedType(Map::class.java, Any::class.java, Item::class.java)
        )

    @Before fun setUp() {
        factory = KeySupplyingJsonAdapterFactory()
        moshi = Moshi.Builder()
            .add(factory)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /* Read */

    @Test fun `reads list of models with keys from a map`() {
        val map = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        assertThat(adapterOfListOfItem.fromJsonValue(map)).containsExactly(
            Item("a", "A"),
            Item("b", "B")
        ).inOrder()
    }

    @Test fun `reads map of models with keys from a map`() {
        val map = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        assertThat(adapterOfMapOfItem.fromJsonValue(map)).isEqualTo(
            mapOf(
                "a" to Item("a", "A"),
                "b" to Item("b", "B")
            )
        )
    }

    @Test(expected = JsonDataException::class)
    fun `fails to read a list of models with keys from a list`() {
        val list = listOf(
            mapOf("name" to "A"),
            mapOf("name" to "B")
        )
        adapterOfListOfItem.fromJsonValue(list)
    }

    @Test(expected = JsonDataException::class)
    fun `fails to read a map of models with keys from a list`() {
        val list = listOf(
            mapOf("name" to "A"),
            mapOf("name" to "B")
        )
        adapterOfMapOfItem.fromJsonValue(list)
    }

    @Test fun `does not attempt to read non-annotated type`() {
        val type = newParameterizedType(List::class.java, ItemNonAnnotated::class.java)
        assertThat(factory.create(type, emptySet(), moshi)).isNull()
    }

    /* Write */

    @Test fun `writes list of models with keys omitted to a map`() {
        val list = listOf(
            Item("a", "A"),
            Item("b", "B")
        )
        assertThat(adapterOfListOfItem.toJsonValue(list)).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    @Test fun `writes map of models with keys omitted to a map`() {
        val map = mapOf(
            "a" to Item("a", "A"),
            "b" to Item("b", "B")
        )
        assertThat(adapterOfMapOfItem.toJsonValue(map)).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    /* Misc */

    @Test(expected = JsonDataException::class)
    fun `presence of special key results in error if it would override a property from the input data`() {
        val input = mapOf("a" to mapOf("name" to "A", "_key" to "42"))
        adapterOfCollectionOfItem.fromJsonValue(input)
    }

    @Test fun `reads and writes are null-safe`() {
        assertThat(adapterOfItem.toJsonValue(null)).isNull()
        assertThat(adapterOfListOfItem.toJsonValue(null)).isNull()
        assertThat(adapterOfMapOfItem.toJsonValue(null)).isNull()
        assertThat(adapterOfItem.fromJsonValue(null)).isNull()
        assertThat(adapterOfListOfItem.fromJsonValue(null)).isNull()
        assertThat(adapterOfMapOfItem.fromJsonValue(null)).isNull()
    }

    @RequiresKey
    data class Item(
        @Json(name = "_key")
        val id: String,
        @Json(name = "name")
        val name: String
    )

    data class ItemNonAnnotated(
        @Json(name = "name")
        val name: String
    )
}
