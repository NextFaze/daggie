package com.nextfaze.daggie.manup

import android.content.Context

internal val Context.versionCode: Int get() = packageManager.getPackageInfo(packageName, 0).versionCode