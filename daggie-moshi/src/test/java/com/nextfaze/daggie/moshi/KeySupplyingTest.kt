@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.moshi

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Json
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import org.junit.Test

class KeySupplyingTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(KeySupplyingJsonAdapterFactory())
        .build()

    private val adapterOfListOfItem
        get() = moshi.adapter<List<Item>>(newParameterizedType(List::class.java, Item::class.java))
    private val adapterOfSetOfItem
        get() = moshi.adapter<Set<Item>>(newParameterizedType(Set::class.java, Item::class.java))
    private val adapterOfCollectionOfItem
        get() = moshi.adapter<Collection<Item>>(newParameterizedType(Collection::class.java, Item::class.java))
    private val adapterOfMapOfItem
        get() = moshi.adapter<Map<String, Item>>(newParameterizedType(Map::class.java, Any::class.java, Item::class.java))

    @Test fun `read list of models from a map with their respective keys`() {
        val input = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        val output = adapterOfListOfItem.fromJsonValue(input)
        assertThat(output).isEqualTo(
            listOf(
                Item("a", "A"),
                Item("b", "B")
            )
        )
    }

    @Test fun `read set of models from a map with their respective keys`() {
        val input = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        val output = adapterOfSetOfItem.fromJsonValue(input)
        assertThat(output).isEqualTo(
            setOf(
                Item("a", "A"),
                Item("b", "B")
            )
        )
    }

    @Test fun `read collection of models from a map with their respective keys`() {
        val input = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        val output = adapterOfCollectionOfItem.fromJsonValue(input)
        assertThat(output).isEqualTo(
            listOf(
                Item("a", "A"),
                Item("b", "B")
            )
        )
    }

    @Test fun `read map of models from a map with their respective keys`() {
        val input = mapOf(
            "a" to mapOf("name" to "A"),
            "b" to mapOf("name" to "B")
        )
        val output = adapterOfMapOfItem.fromJsonValue(input)
        assertThat(output).isEqualTo(
            mapOf(
                "a" to Item("a", "A"),
                "b" to Item("b", "B")
            )
        )
    }

    @Test fun `read null list of models`() = assertThat(adapterOfListOfItem.fromJsonValue(null)).isNull()

    @Test fun `read null set of models`() = assertThat(adapterOfSetOfItem.fromJsonValue(null)).isNull()

    @Test fun `read null collection of models`() = assertThat(adapterOfCollectionOfItem.fromJsonValue(null)).isNull()

    @Test fun `write list of models to a map excluding the synthetic keys`() {
        val input = listOf(
            Item("a", "A"),
            Item("b", "B")
        )
        val output = adapterOfListOfItem.toJsonValue(input)
        assertThat(output).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    @Test fun `write set of models to a map excluding the synthetic keys`() {
        val input = setOf(
            Item("a", "A"),
            Item("b", "B")
        )
        val output = adapterOfSetOfItem.toJsonValue(input)
        assertThat(output).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    @Test fun `write collection of models to a map excluding the synthetic keys`() {
        val input = listOf(
            Item("a", "A"),
            Item("b", "B")
        )
        val output = adapterOfCollectionOfItem.toJsonValue(input)
        assertThat(output).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    @Test fun `write map of models to a map excluding the synthetic keys`() {
        val input = mapOf(
            "a" to Item("a", "A"),
            "b" to Item("b", "B")
        )
        val output = adapterOfMapOfItem.toJsonValue(input)
        assertThat(output).isEqualTo(
            mapOf(
                "a" to mapOf("name" to "A"),
                "b" to mapOf("name" to "B")
            )
        )
    }

    @Test fun `write null list of models`() = assertThat(adapterOfListOfItem.toJsonValue(null)).isNull()

    @Test fun `write null set of models`() = assertThat(adapterOfSetOfItem.toJsonValue(null)).isNull()

    @Test fun `write null collection of models`() = assertThat(adapterOfCollectionOfItem.toJsonValue(null)).isNull()

    @Test fun `read collection of nullable primitives from a map`() {
        val input = mapOf("c" to "C", "d" to "D", "e" to null)
        val adapter =
            moshi.adapter<Collection<String?>>(newParameterizedType(Collection::class.java, String::class.java))
        val output = adapter.fromJsonValue(input)
        assertThat(output).isEqualTo(listOf("C", "D", null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `special key throws if it would otherwise override a property from the input data`() {
        val input = mapOf("a" to mapOf("name" to "A", "_key" to "42"))
        adapterOfCollectionOfItem.fromJsonValue(input)
    }

    data class Item(
        @Json(name = "_key")
        val id: Any,
        @Json(name = "name")
        val name: String
    )
}
