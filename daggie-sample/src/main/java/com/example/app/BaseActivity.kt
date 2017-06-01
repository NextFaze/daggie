package com.example.app

import com.nextfaze.daggie.DaggerActivity
import com.nextfaze.daggie.Injector
import com.nextfaze.daggie.applicationComponent

abstract class BaseActivity : DaggerActivity<Injector>() {
    override val injector get() = applicationComponent
}