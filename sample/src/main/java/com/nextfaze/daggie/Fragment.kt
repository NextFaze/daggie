package com.nextfaze.daggie

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import com.trello.rxlifecycle.components.support.RxAppCompatDialogFragment
import com.trello.rxlifecycle.components.support.RxFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(FragmentModule::class))
@FragmentScope
interface FragmentComponent : FragmentInjector, DialogFragmentInjector {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun fragment(fragment: Fragment): Builder
        fun build(): FragmentComponent
    }
}

@Module
class FragmentModule

// TODO: Reduce/eliminate the below code duplication.

abstract class DaggerFragment : RxFragment() {
    lateinit var fragmentComponent: FragmentComponent private set

    val fragmentInjector: FragmentInjector get() = fragmentComponent
    val dialogFragmentInjector: DialogFragmentInjector get() = fragmentComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent = createFragmentComponent()
        inject(fragmentComponent)
    }

    protected abstract fun inject(injector: FragmentInjector)
}

abstract class DaggerDialogFragment : RxAppCompatDialogFragment() {
    lateinit var fragmentComponent: FragmentComponent private set

    val fragmentInjector: FragmentInjector get() = fragmentComponent
    val dialogFragmentInjector: DialogFragmentInjector get() = fragmentComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent = createFragmentComponent()
        inject(fragmentComponent)
    }

    protected abstract fun inject(injector: DialogFragmentInjector)
}

abstract class DaggerAlertDialogFragment : RxAppCompatDialogFragment() {
    lateinit var fragmentComponent: FragmentComponent private set

    val fragmentInjector: FragmentInjector get() = fragmentComponent
    val dialogFragmentInjector: DialogFragmentInjector get() = fragmentComponent

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentComponent = createFragmentComponent()
        inject(fragmentComponent)
    }

    protected abstract fun inject(injector: DialogFragmentInjector)
}

private fun Fragment.createFragmentComponent() = (activity as DaggerActivity)
        .retainedComponent.fragmentComponentBuilder().fragment(this).build()