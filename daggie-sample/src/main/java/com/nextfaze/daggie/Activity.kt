package com.nextfaze.daggie

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

abstract class DaggerActivity<I> : RxAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}
