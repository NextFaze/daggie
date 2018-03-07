@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.moshi

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Json
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import org.junit.Test

class KeyValueParsingTest {

    private val moshi = Moshi.Builder()
        .add(KeySupplyingJsonAdapterFactory())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapterOfItem
        get() = moshi.adapter<Item>(Item::class.java)
    private val adapterOfMapOfItem
        get() = moshi.adapter<Map<String, Item>>(newParameterizedType(Map::class.java, String::class.java, Item::class.java))

    @Test fun `key is made available to be parsed`() {
        assertThat(adapterOfItem.fromKeyValuePair("1", mapOf("name" to "A"))).isEqualTo(Item("1", "A"))
    }

    @Test fun `key is not inserted when target type is a collection`() {
        val inputMap = mapOf(
            "1" to mapOf("name" to "A"),
            "2" to mapOf("name" to "B")
        )
        val outputMap = mapOf(
            "1" to Item("1", "A"),
            "2" to Item("2", "B")
        )
        assertThat(adapterOfMapOfItem.fromKeyValuePair("5", inputMap)).isEqualTo(outputMap)
    }

    @Test fun `key is absent when serialized`() {
        assertThat(adapterOfItem.toJsonValue(Item("2", "B"))).isEqualTo(mapOf("name" to "B"))
    }

    @RequiresKey
    data class Item(
        @Json(name = "_key")
        val id: String,
        @Json(name = "name")
        val name: String
    )
}
