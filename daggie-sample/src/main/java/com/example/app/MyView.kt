package com.example.app

import android.app.NotificationManager
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.nextfaze.daggie.injector
import javax.inject.Inject

class MyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    @Inject lateinit internal var notificationManager: NotificationManager

    init {
        injector.inject(this)
        setBackgroundColor(0xFF00FF)
    }
}