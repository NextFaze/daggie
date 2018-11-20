package com.nextfaze.daggie.rxpreferences

import com.f2prateek.rx.preferences2.Preference
import com.nextfaze.daggie.optional.Optional
import com.nextfaze.daggie.optional.toOptional
import com.nextfaze.daggie.optional.value
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Returns a property delegate for a read/write property that manipulates the non-null value of this [Preference]. */
fun <T : Any> Preference<T>.propertyDelegate() = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
}

/** Returns a property delegate for a read/write property that manipulates the nullable value of this [Preference]. */
@JvmName("optionalPropertyDelegate")
fun <T : Any> Preference<Optional<T>>.propertyDelegate() = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = get().value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = set(value.toOptional())
}
