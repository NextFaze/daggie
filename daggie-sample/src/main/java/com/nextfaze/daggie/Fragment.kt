package com.nextfaze.daggie

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDialogFragment

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
