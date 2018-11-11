package com.nextfaze.daggie

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment

abstract class DaggerFragment<I> : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}

abstract class DaggerDialogFragment<I> : AppCompatDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}

val Fragment.applicationComponent get() = context?.applicationComponent
