package com.nextfaze.daggie

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(ActivityModule::class))
@ActivityScope
interface ActivityComponent : ActivityInjector, DialogInjector, ViewInjector {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun activity(activity: AppCompatActivity): Builder
        fun build(): ActivityComponent
    }
}

@Module class ActivityModule {
    @Provides fun activity(activity: AppCompatActivity): Activity = activity
    @Provides fun fragmentActivity(activity: AppCompatActivity): FragmentActivity = activity
    @Provides fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager!!
}

abstract class DaggerActivity : RxAppCompatActivity() {
    lateinit var retainedComponent: RetainedComponent private set
    lateinit var activityComponent: ActivityComponent private set

    val activityInjector: ActivityInjector get() = activityComponent
    val dialogInjector: DialogInjector get() = activityComponent
    val viewInjector: ViewInjector get() = activityComponent

    val actionProviderInjector: ActionProviderInjector get() = retainedComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        retainedComponent = (lastCustomNonConfigurationInstance as? RetainedComponent) ?:
                applicationComponent.retainedComponent()
        activityComponent = retainedComponent.activityComponentBuilder().activity(this).build()
        inject(activityComponent)
        super.onCreate(savedInstanceState)
    }

    /**
     * We don't use this anywhere else so it's final for now so it can't accidentally be overridden, but if it's needed
     * then some sort of container object or something will be required.
     */
    override fun onRetainCustomNonConfigurationInstance(): Any = retainedComponent

    /**
     * Ideally this should only be called once per activity instance.
     * Calling it more than once (or more specifically, calling `injector.inject(this);` more than once) will
     * result in duplicate inject calls to super classes.
     * For most injects this wont be a problem, but for unscoped objects (or those with scopes smaller than the object
     * being injected), it will result in multiple objects being created and discarded for each extra inject call.
     * In reality this is unlikely to matter much, however if an object adds listeners in its constructor (i.e. holds a
     * reference to the object being inject), and is expecting to be told when to unregister, then it may never get
     * that callback as the object will be re-injected on the subsequent inject call(s).
     *
     *
     * TL;DR; Don't call super when overriding this method.
     *
     *
     * TODO: There's probably a nicer way to enforce this.
     */
    protected abstract fun inject(injector: ActivityInjector)
}
