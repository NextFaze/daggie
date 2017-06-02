package com.nextfaze.daggie

import com.example.app.BuildTypeInjector
import com.example.app.BuildTypeModule
import com.example.app.MainActivity
import com.example.app.MainModule
import com.example.app.MyView
import com.example.app.UserScopeComponent
import com.example.app.UserScopeManager
import dagger.Module

/** Performs member injection on behalf of the [ApplicationComponent]. */
interface Injector : BuildTypeInjector {
    fun inject(activity: MainActivity)
    fun inject(v: MyView)
}

/** Contributes app-specific member functions to the [ApplicationComponent]. */
interface AppMembers {
    fun userScopeManager(): UserScopeManager
}

/** Provides app-specific bindings and subcomponents to the [ApplicationComponent]. */
@Module(
        includes = arrayOf(
                MainModule::class,
                BuildTypeModule::class
        ),
        subcomponents = arrayOf(
                UserScopeComponent::class
        )
)
class AppModule