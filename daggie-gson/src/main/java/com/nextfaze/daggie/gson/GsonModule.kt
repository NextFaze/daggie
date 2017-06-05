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
 * * `Configurator<GsonBuilder>` set bindings to append additional configuration to the [GsonBuilder]
 * * `Ordered<TypeAdapterFactory>` set bindings to register [type adapter factories][TypeAdapterFactory] in the order
 * defined by [Ordered.order]
 * * `TypeAdapterFactory` set bindings to register type adapter factories in an undefined order AFTER the ordered
 * type adapter factories
 */
@Module class GsonModule {
    @Provides @Singleton internal fun gson(
            orderedTypeAdapterFactories: Set<@JvmSuppressWildcards Ordered<TypeAdapterFactory>>,
            unorderedTypeAdapterFactories: Set<@JvmSuppressWildcards TypeAdapterFactory>,
            configurators: Set<@JvmSuppressWildcards Configurator<GsonBuilder>>
    ) = GsonBuilder().apply {
        orderedTypeAdapterFactories.asSequence().sorted().map { it.value }.forEach { registerTypeAdapterFactory(it) }
        unorderedTypeAdapterFactories.forEach { registerTypeAdapterFactory(it) }
        configurators.forEach { it(this) }
    }.create()!!

    @Provides @ElementsIntoSet internal fun defaultUnorderedTypeAdapterFactories() = emptySet<TypeAdapterFactory>()

    @Provides @ElementsIntoSet internal fun defaultOrderedTypeAdapterFactories() = emptySet<Ordered<TypeAdapterFactory>>()

    @Provides @ElementsIntoSet internal fun defaultGsonBuilderConfigurators() = emptySet<Configurator<GsonBuilder>>()
}