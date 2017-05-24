package com.nextfaze.daggie

import android.support.v4.view.ActionProvider

val ActionProvider.injector: ActionProviderInjector get() = (context as DaggerActivity).actionProviderInjector