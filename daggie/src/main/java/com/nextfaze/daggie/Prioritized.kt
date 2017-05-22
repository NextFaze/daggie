package com.nextfaze.daggie

/**
 * Used to denote the priority of an object. This is typically used with Dagger set bindings
 * to allow modules to annotate the priority of the binding they contribute to the set.
 * A higher priority means the object takes precedence over a lower priority.
 * @property priority The priority value to associate with [value].
 * @property value The object to annotate with a priority.
 */
data class Prioritized<out T>(val priority: Int, val value: T)

/**
 * Performs the given action on each element of the prioritized `Iterable`, in descending order of
 * priority.
 */
inline fun <T> Iterable<Prioritized<T>>.forEachPrioritized(action: (T) -> Unit) =
        sortedByDescending { it.priority }.forEach { action.invoke(it.value) }