package com.nextfaze.daggie.rxpreferences

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.nextfaze.daggie.optional.None
import com.nextfaze.daggie.optional.Optional
import com.nextfaze.daggie.optional.toOptional
import com.nextfaze.daggie.optional.value

/** Create a preference for type [T] for key [key]. Absent by default. */
fun <T : Any> RxSharedPreferences.getOptionalObject(
    key: String,
    converter: Preference.Converter<T>
): Preference<Optional<T>> = OptionalPreference(getObject(key, None, OptionalConverter(converter)))

/** Create a preference for type [T] for key [key], and with default value [defaultValue]. */
fun <T : Any> RxSharedPreferences.getOptionalObject(
    key: String,
    defaultValue: T,
    converter: Preference.Converter<T>
): Preference<Optional<T>> = OptionalPreference(getObject(key, defaultValue.toOptional(), OptionalConverter(converter)))

/** Accesses the optional value of this preference, supporting `null` values. */
var <T : Any> Preference<Optional<T>>.value
    get(): T? = get().value
    set(value) = set(value.toOptional())

private class OptionalConverter<T : Any>(private val converter: Preference.Converter<T>) :
    Preference.Converter<Optional<T>> {

    // Serialize the optional value, which is always Some, because we ensure the value is deleted when None is written
    override fun serialize(optional: Optional<T>) =
        optional.value?.let { converter.serialize(it) } ?: throw AssertionError()

    override fun deserialize(serialized: String) =
        converter.deserialize(serialized).toOptional()
}

private class OptionalPreference<T : Any>(private val pref: Preference<Optional<T>>) : Preference<Optional<T>> by pref {
    override fun set(value: Optional<T>) {
        if (value === None) delete()
        else pref.set(value)
    }
}
