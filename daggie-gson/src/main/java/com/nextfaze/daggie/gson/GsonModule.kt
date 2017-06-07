package com.nextfaze.daggie.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapterFactory
import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Ordered
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Singleton

/**
 * Provides a [Gson] binding.
 *
 * Users can further configure Gson by providing:
 * * [Ordered]<[Configurator]<[GsonBuilder]>> set bindings to append additional configuration to the `GsonBuilder` in
 * the natural ordering of [Ordered]
 * * [Configurator]<[GsonBuilder]> set bindings to append additional configuration to the `GsonBuilder` in an undefined
 * order AFTER the ordered configurators
 * * [Ordered]<[TypeAdapterFactory]> set bindings to register `TypeAdapterFactory` instances in the natural ordering of
 * [Ordered]
 * * [TypeAdapterFactory] set bindings to register `TypeAdapterFactory` instances in an undefined order AFTER the ordered
 * `TypeAdapterFactory` instances
 */
@Module class GsonModule {
    @Provides @Singleton internal fun gson(
            orderedTypeAdapterFactories: Set<@JvmSuppressWildcards Ordered<TypeAdapterFactory>>,
            unorderedTypeAdapterFactories: Set<@JvmSuppressWildcards TypeAdapterFactory>,
            orderedConfigurators: Set<@JvmSuppressWildcards Ordered<Configurator<GsonBuilder>>>,
            unorderedConfigurators: Set<@JvmSuppressWildcards Configurator<GsonBuilder>>
    ) = GsonBuilder().apply {
        orderedTypeAdapterFactories.asSequence().sorted().map { it.value }.forEach { registerTypeAdapterFactory(it) }
        unorderedTypeAdapterFactories.forEach { registerTypeAdapterFactory(it) }
        orderedConfigurators.asSequence().sorted().map { it.value }.forEach { it(this) }
        unorderedConfigurators.forEach { it(this) }
    }.create()!!

    @Provides @ElementsIntoSet internal fun defaultUnorderedTypeAdapterFactories() = emptySet<TypeAdapterFactory>()

    @Provides @ElementsIntoSet internal fun defaultOrderedTypeAdapterFactories() = emptySet<Ordered<TypeAdapterFactory>>()

    @Provides @ElementsIntoSet internal fun defaultGsonBuilderConfigurators() = emptySet<Configurator<GsonBuilder>>()
}