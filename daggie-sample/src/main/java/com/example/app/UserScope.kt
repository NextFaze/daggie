package com.example.app

import android.view.View
import com.nextfaze.daggie.DaggerActivity
import com.nextfaze.daggie.applicationComponent
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Scope
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Scope
@MustBeDocumented
@Retention(BINARY)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, CLASS, FILE)
annotation class UserScope

@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class UserLocal

@Subcomponent(modules = [UserScopeModule::class])
@UserScope
interface UserScopeComponent : UserScopeInjector {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun user(user: User): Builder
        fun build(): UserScopeComponent
    }
}

@Module class UserScopeModule {
    @Provides @UserLocal internal fun userName(user: User) = user.name
}

interface UserScopeInjector {
    fun inject(view: CurrentUserView)
    fun inject(activity: ProfileActivity)
}

@Singleton class UserScopeManager @Inject constructor(private val userComponentBuilder: UserScopeComponent.Builder) {

    var user: User? = null

    val injector: UserScopeInjector get() = userComponentBuilder.user(user!!).build()
}

val View.userScopeInjector get() = applicationComponent.userScopeManager().injector

abstract class UserScopeActivity : DaggerActivity<UserScopeInjector>() {
    override val injector get() = applicationComponent.userScopeManager().injector
}
