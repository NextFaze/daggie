package com.nextfaze.daggie

/**
 * Used to annotate an object with an ordering relative to others of the same type. This is intended to be used with
 * Dagger set bindings to allow modules to control the position of their binding in the set.
 * @property order The index value to associate with [value].
 * @property value The object to annotate with an order.
 */
data class Ordered<T>(val order: Int, val value: T) : Comparable<Ordered<T>> {
    override fun compareTo(other: Ordered<T>) = order.compareTo(other.order)
}