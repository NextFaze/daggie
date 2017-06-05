package com.example.app

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.nextfaze.daggie.Injector
import com.nextfaze.daggie.sample.R
import com.nextfaze.daggie.slf4j.d
import com.nextfaze.daggie.slf4j.logger
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    private val log = logger()

    @Inject lateinit internal var notificationManager: NotificationManager

    @Inject lateinit internal var userScopeManager: UserScopeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        userScopeManager.user = User("Joe")
        userProfileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        Glide.with(this).load("http://thecatapi.com/api/images/get?format=src&type=png").into(imageView)
        log.d { "Test" }
    }

    override fun inject(injector: Injector) = injector.inject(this)
}