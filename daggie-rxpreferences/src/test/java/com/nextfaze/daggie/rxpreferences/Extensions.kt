package com.nextfaze.daggie.rxpreferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences

/**
 * Create an instance of [RxSharedPreferences] for this [SharedPreferences].
 * @see RxSharedPreferences.create
 */
fun SharedPreferences.toRxSharedPreferences(): RxSharedPreferences = RxSharedPreferences.create(this)
