package com.nextfaze.daggie

import androidx.core.view.ActionProvider

val ActionProvider.applicationComponent get() = context.applicationComponent

val ActionProvider.injector get() = context.injector
