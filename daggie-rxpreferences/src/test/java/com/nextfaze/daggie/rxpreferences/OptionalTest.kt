package com.nextfaze.daggie.rxpreferences

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nextfaze.daggie.optional.None
import com.nextfaze.daggie.optional.Optional
import com.nextfaze.daggie.optional.toOptional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = RobolectricApplication::class)
class OptionalTest {

    private val backing =
        ApplicationProvider.getApplicationContext<Application>().getSharedPreferences("test", Context.MODE_PRIVATE)
    private val prefs = backing.toRxSharedPreferences()

    private lateinit var pref: Preference<Optional<Item>>

    @Before fun setUp() {
        pref = prefs.getOptionalObject(KEY, ItemConverter)
    }

    @Test fun `when value set to non-null, backing prefs should contain key`() {
        pref.value = Item("a")
        assertThat(backing.all).containsKey(KEY)
    }

    @Test fun `when value set to null, backing prefs should not contain key`() {
        pref.value = Item("a")
        pref.value = null
        assertThat(backing.all).doesNotContainKey(KEY)
    }

    @Test fun `when value set to none, backing prefs should not contain key`() {
        pref.value = Item("a")
        pref.set(None)
        assertThat(backing.all).doesNotContainKey(KEY)
    }

    @Test fun `when value deleted, backing prefs should not contain key`() {
        pref.value = Item("a")
        pref.delete()
        assertThat(backing.all).doesNotContainKey(KEY)
    }

    @Test fun `given pref with default value, and no value, when value loaded, it should match default value`() {
        val defaultValue = Item("d")
        val pref = prefs.getOptionalObject(KEY, defaultValue, ItemConverter)
        assertThat(pref.get()).isEqualTo(defaultValue.toOptional())
    }

    private data class Item(val name: String)

    private object ItemConverter : Preference.Converter<Item> {
        override fun serialize(value: Item): String = value.name
        override fun deserialize(serialized: String): Item = Item(name = serialized)
    }

    companion object {
        private const val KEY = "k"
    }
}

