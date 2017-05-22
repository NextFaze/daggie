package com.nextfaze.daggie

import android.app.IntentService
import android.app.Service

abstract class DaggerService : Service() {
    override fun onCreate() {
        super.onCreate()
        applicationComponent.let { inject(it) }
    }

    protected abstract fun inject(injector: ServiceInjector)
}

abstract class DaggerIntentService(val name: String) : IntentService(name) {
    override fun onCreate() {
        super.onCreate()
        applicationComponent.let { inject(it) }
    }

    protected abstract fun inject(injector: ServiceInjector)
}