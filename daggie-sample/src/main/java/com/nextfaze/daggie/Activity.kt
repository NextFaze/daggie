package com.nextfaze.daggie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class DaggerActivity<I> : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        inject(injector)
        super.onCreate(savedInstanceState)
    }

    protected abstract val injector: I

    protected abstract fun inject(injector: I)
}
