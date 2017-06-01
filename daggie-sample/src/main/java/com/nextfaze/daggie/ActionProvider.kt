package com.nextfaze.daggie

import android.support.v4.view.ActionProvider

val ActionProvider.applicationComponent get() = context.applicationComponent

val ActionProvider.injector get() = context.injector