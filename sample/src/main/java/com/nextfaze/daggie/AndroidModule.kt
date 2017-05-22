package com.nextfaze.daggie

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Context.*
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper.getMainLooper
import android.os.PowerManager
import android.os.Vibrator
import android.os.storage.StorageManager
import android.telephony.TelephonyManager
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import dagger.Module
import dagger.Provides

/** Provides Android system service bindings from the [Application] context. */
@Module class AndroidModule {
    @Provides internal fun context(application: Application): Context = application
    @Provides internal fun assetManager(application: Application): AssetManager = application.assets
    @Provides internal fun contentResolver(application: Application): ContentResolver = application.contentResolver
    @Provides internal fun packageManager(application: Application): PackageManager = application.packageManager
    @Provides internal fun resources(application: Application): Resources = application.resources
    @Provides internal fun handler(): Handler = Handler(getMainLooper())
    @Provides internal fun geocoder(application: Application): Geocoder = Geocoder(application)
    @Provides internal fun activityManager(application: Application): ActivityManager = application.systemService(ACTIVITY_SERVICE)
    @Provides internal fun audioManager(application: Application): AudioManager = application.systemService(AUDIO_SERVICE)
    @Provides internal fun alarmManager(application: Application): AlarmManager = application.systemService(ALARM_SERVICE)
    @Provides internal fun connectivityManager(application: Application): ConnectivityManager = application.systemService(CONNECTIVITY_SERVICE)
    @Provides internal fun downloadManager(application: Application): DownloadManager = application.systemService(DOWNLOAD_SERVICE)
    @Provides internal fun inputMethodManager(application: Application): InputMethodManager = application.systemService(INPUT_METHOD_SERVICE)
    @Provides internal fun keyguardManager(application: Application): KeyguardManager = application.systemService(KEYGUARD_SERVICE)
    @Provides internal fun locationManager(application: Application): LocationManager = application.systemService(LOCATION_SERVICE)
    @Provides internal fun notificationManager(application: Application): NotificationManager = application.systemService(NOTIFICATION_SERVICE)
    @Provides internal fun powerService(application: Application): PowerManager = application.systemService(POWER_SERVICE)
    @Provides internal fun searchManager(application: Application): SearchManager = application.systemService(SEARCH_SERVICE)
    @Provides internal fun sensorManager(application: Application): SensorManager = application.systemService(SENSOR_SERVICE)
    @Provides internal fun storageManager(application: Application): StorageManager = application.systemService(STORAGE_SERVICE)
    @Provides internal fun telephonyManager(application: Application): TelephonyManager = application.systemService(TELEPHONY_SERVICE)
    @Provides internal fun uiModeManager(application: Application): UiModeManager = application.systemService(UI_MODE_SERVICE)
    @Provides internal fun vibrator(application: Application): Vibrator = application.systemService(VIBRATOR_SERVICE)
    @Provides internal fun wifiManager(application: Application): WifiManager = application.systemService(WIFI_SERVICE)
    @Provides internal fun windowManager(application: Application): WindowManager = application.systemService(WINDOW_SERVICE)

    private inline fun <reified T> Application.systemService(name: String): T = (getSystemService(name) as? T)!!
}
