@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.moshi

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

class MapParsingTest {

    private lateinit var factory: MapParsingJsonAdapterFactory

    private lateinit var moshi: Moshi

    private val adapterOfListOfString
        get() = moshi.adapter<List<String>>(newParameterizedType(List::class.java, String::class.java))
    private val adapterOfSetOfString
        get() = moshi.adapter<Set<String>>(newParameterizedType(Set::class.java, String::class.java))

    @Before fun setUp() {
        factory = MapParsingJsonAdapterFactory()
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(factory)
            .build()
    }

    @Test fun `reads json object as collection`() {
        @Language("JSON")
        val json = """{
            "0": "A",
            "1": "B",
            "2": "B"
        }"""
        val list = adapterOfListOfString.fromJson(json)
        assertThat(list).isEqualTo(listOf("A", "B", "B"))
        val set = adapterOfSetOfString.fromJson(json)
        assertThat(set).isEqualTo(setOf("A", "B"))
    }

    @Test fun `does not attempt to read as map if input is array`() {
        @Language("JSON")
        val json = """["A", "B", "C"]"""
        val list = adapterOfListOfString.fromJson(json)
        assertThat(list).isEqualTo(listOf("A", "B", "C"))
    }

    @Test fun `order is preserved when written`() {
        @Language("JSON")
        val inputJson = """{
            "0": "B",
            "1": "A",
            "2": "A"
        }"""
        val set = adapterOfSetOfString.fromJson(inputJson)
        val rewrittenJson = adapterOfSetOfString.toJson(set)
        //language=JSON
        JSONAssert.assertEquals("""["B", "A"]""", rewrittenJson, true)
    }

    @Test fun `reads and writes are null-safe`() {
        assertThat(adapterOfListOfString.fromJson("null")).isNull()
        assertThat(adapterOfSetOfString.fromJson("null")).isNull()
        assertThat(adapterOfListOfString.toJson(null)).isEqualTo("null")
        assertThat(adapterOfSetOfString.toJson(null)).isEqualTo("null")
    }
}

