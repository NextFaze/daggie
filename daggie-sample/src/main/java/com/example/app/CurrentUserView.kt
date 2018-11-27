package com.example.app

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class CurrentUserView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    @Inject lateinit var user: User

    init {
        userScopeInjector.inject(this)
        text = "User: ${user.name}"
    }
}
