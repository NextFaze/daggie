package com.nextfaze.daggie.rxrelay

import com.jakewharton.rxrelay2.BehaviorRelay
import com.nextfaze.daggie.optional.Optional
import com.nextfaze.daggie.optional.toOptional
import com.nextfaze.daggie.optional.value
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Returns a property delegate for a read/write property that allows modifying the non-null
 * [value][BehaviorRelay.getValue] of this relay.
 */
fun <T : Any> BehaviorRelay<T>.propertyDelegate() = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value!!
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = accept(value)
}

/**
 * Returns a property delegate for a read/write property that allows modifying the nullable
 * [value][BehaviorRelay.getValue] of this relay.
 */
@JvmName("optionalPropertyDelegate")
fun <T : Any> BehaviorRelay<Optional<T>>.propertyDelegate() = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value?.value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = accept(value.toOptional())
}
