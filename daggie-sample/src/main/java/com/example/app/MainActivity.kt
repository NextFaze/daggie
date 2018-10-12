package com.example.app

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.nextfaze.daggie.Injector
import com.nextfaze.daggie.autodispose.scope
import com.nextfaze.daggie.permissions.Permissions
import com.nextfaze.daggie.sample.R
import com.nextfaze.daggie.slf4j.d
import com.nextfaze.daggie.slf4j.logger
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    private val log = logger()

    @Inject lateinit internal var notificationManager: NotificationManager

    @Inject lateinit internal var userScopeManager: UserScopeManager

    @Inject lateinit internal var permissions: Permissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        userScopeManager.user = User("Joe")
        userProfileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        imageView.uri = Uri.parse("http://thecatapi.com/api/images/get?format=src&type=png")
        log.d { "Test" }
        permissions.requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION).autoDisposable(scope()).subscribe()
    }

    override fun inject(injector: Injector) = injector.inject(this)
}
