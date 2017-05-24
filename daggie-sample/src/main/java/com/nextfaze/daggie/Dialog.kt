package com.nextfaze.daggie

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatDialog

abstract class DaggerDialog : AppCompatDialog {

    @JvmOverloads
    constructor(context: Context, theme: Int = 0) : super(context, theme)

    constructor(context: Context, cancelable: Boolean = true, cancelListener: DialogInterface.OnCancelListener? = null) :
            super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject((context as DaggerActivity).dialogInjector)
    }

    protected abstract fun inject(injector: DialogInjector)
}
