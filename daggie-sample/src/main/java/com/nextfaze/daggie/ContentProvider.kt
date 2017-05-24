package com.nextfaze.daggie

import android.content.ContentProvider
import android.content.Context

abstract class DaggerContentProvider : ContentProvider() {
    final override fun onCreate(): Boolean {
        // Instant run
        if (context::class.java.name.endsWith("BootstrapApplication")) {
            doInject(context::class.java.getDeclaredField("realApplication").apply { isAccessible = true }.get(context) as Context)
        } else {
            doInject(context)
        }
        return true
    }

    private fun doInject(context: Context) {
        context.applicationComponent.let { inject(it); onInjected() }
    }

    /**
     * Be aware that this method will be called **before** [Application#onCreate()], though *after*
     * [Application#attachBaseContext(Context)], and only if dagger has been initialized.
     */
    protected open fun onInjected() {
    }

    protected abstract fun inject(injector: ContentProviderInjector)
}
