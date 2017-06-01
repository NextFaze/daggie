package com.example.app

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class CurrentUserView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        TextView(context, attrs, defStyleAttr) {

    @Inject lateinit internal var user: User

    init {
        userScopeInjector.inject(this)
        text = "User: ${user.name}"
    }
}