package com.example.app

import android.os.Bundle
import com.nextfaze.daggie.sample.R
import com.nextfaze.daggie.slf4j.d
import com.nextfaze.daggie.slf4j.logger
import javax.inject.Inject

class ProfileActivity : UserScopeActivity() {

    private val log = logger()

    @Inject lateinit internal var user: User

    @Inject @field:UserLocal lateinit internal var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        log.d { "Current user name: $userName" }
    }

    override fun inject(injector: UserScopeInjector) = injector.inject(this)
}