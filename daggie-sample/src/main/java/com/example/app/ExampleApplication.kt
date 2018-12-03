package com.example.app

import android.annotation.SuppressLint
import com.nextfaze.daggie.DaggerApplication
import com.nextfaze.daggie.Foreground
import com.nextfaze.daggie.injector
import com.nextfaze.daggie.slf4j.*
import io.reactivex.Observable
import javax.inject.Inject

class ExampleApplication : DaggerApplication() {

    private val log = logger()

    @Inject @field:Foreground
    lateinit var foreground: Observable<Boolean>

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
        foreground.subscribe { log.d { "Foreground = $it" } }
    }
}
