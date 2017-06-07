package com.nextfaze.daggie.moshi

import com.nextfaze.daggie.Configurator
import com.nextfaze.daggie.Ordered
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Singleton

/**
 * Provides a [Moshi] binding.
 *
 * Users can further configure Moshi by providing:
 * * [Ordered]<[Configurator]<[Moshi.Builder]>> set bindings to append additional configuration to the `Moshi.Builder`
 * in the natural ordering of [Ordered]
 * * [Configurator]<[Moshi.Builder]> set bindings to append additional configuration to the `Moshi.Builder` in an
 * undefined order AFTER the ordered configurators
 * * [Ordered]<[JsonAdapter.Factory]> set bindings to register `JsonAdapter.Factory` instances in the natural ordering
 * of [Ordered]
 * * [JsonAdapter.Factory] set bindings to register JSON adapter factories in an undefined order AFTER the ordered
 * `JsonAdapter.Factory` instances
 */
@Module class MoshiModule {
    @Provides @Singleton internal fun moshi(
            orderedJsonAdapterFactories: Set<@JvmSuppressWildcards Ordered<JsonAdapter.Factory>>,
            unorderedJsonAdapterFactories: Set<@JvmSuppressWildcards JsonAdapter.Factory>,
            orderedConfigurators: Set<@JvmSuppressWildcards Ordered<Configurator<Moshi.Builder>>>,
            unorderedConfigurators: Set<@JvmSuppressWildcards Configurator<Moshi.Builder>>
    ) = Moshi.Builder().apply {
        orderedJsonAdapterFactories.asSequence().sorted().map { it.value }.forEach { add(it) }
        unorderedJsonAdapterFactories.forEach { add(it) }
        orderedConfigurators.asSequence().sorted().map { it.value }.forEach { it(this) }
        unorderedConfigurators.forEach { it(this) }
    }.build()!!

    @Provides @ElementsIntoSet internal fun defaultOrderedJsonAdapterFactories() =
            emptySet<@JvmSuppressWildcards Ordered<JsonAdapter.Factory>>()

    @Provides @ElementsIntoSet internal fun defaultUnorderedJsonAdapterFactories() =
            emptySet<@JvmSuppressWildcards JsonAdapter.Factory>()

    @Provides @ElementsIntoSet internal fun defaultOrderedMoshiBuilderConfigurators() =
            emptySet<@JvmSuppressWildcards Ordered<Configurator<Moshi.Builder>>>()

    @Provides @ElementsIntoSet internal fun defaultUnorderedMoshiBuilderConfigurators() =
            emptySet<@JvmSuppressWildcards Configurator<Moshi.Builder>>()
}