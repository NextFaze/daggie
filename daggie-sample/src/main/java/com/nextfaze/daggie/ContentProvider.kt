package com.nextfaze.daggie

import android.content.ContentProvider
import android.support.annotation.CallSuper

abstract class DaggerContentProvider : ContentProvider() {
    @CallSuper override fun onCreate(): Boolean {
        inject(applicationComponent)
        return true
    }

    protected abstract fun inject(injector: Injector)
}

val ContentProvider.applicationComponent get() = context.applicationComponent