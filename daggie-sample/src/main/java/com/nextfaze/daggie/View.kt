package com.nextfaze.daggie

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View

val View.injector: ViewInjector get() = (context.activity as DaggerActivity).viewInjector

private val Context.activity: Activity?
    get() {
        when {
            this is Activity -> return this
            this is ContextWrapper -> return this.baseContext.activity
            else -> return null
        }
    }