package com.nextfaze.daggie

import dagger.Subcomponent

@Subcomponent
@RetainedScope
interface RetainedComponent : ActionProviderInjector {
    fun activityComponentBuilder(): ActivityComponent.Builder
    fun fragmentComponentBuilder(): FragmentComponent.Builder
}
