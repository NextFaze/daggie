package com.nextfaze.daggie

import android.view.View

val View.applicationComponent get() = context.applicationComponent

val View.injector get() = context.injector