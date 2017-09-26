package com.nextfaze.daggie

import android.os.Bundle
import android.support.v4.app.Fragment
import com.trello.rxlifecycle2.components.support.RxAppCompatDialogFragment
import com.trello.rxlifecycle2.components.support.RxFragment

abstract class DaggerFragment<I> : RxFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}

abstract class DaggerDialogFragment<I> : RxAppCompatDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}

val Fragment.applicationComponent get() = context?.applicationComponent
