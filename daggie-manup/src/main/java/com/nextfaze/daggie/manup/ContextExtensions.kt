package com.nextfaze.daggie.manup

import android.content.Context

@Suppress("DEPRECATION")
internal val Context.versionCode: Int get() = packageManager.getPackageInfo(packageName, 0).versionCode
