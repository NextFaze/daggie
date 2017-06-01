package com.nextfaze.daggie

import android.app.IntentService
import android.app.Service

abstract class DaggerService : Service() {
    override fun onCreate() {
        inject(applicationComponent)
        super.onCreate()
    }

    protected abstract fun inject(injector: Injector)
}

abstract class DaggerIntentService(val name: String) : IntentService(name) {
    override fun onCreate() {
        inject(applicationComponent)
        super.onCreate()
    }

    protected abstract fun inject(injector: Injector)
}