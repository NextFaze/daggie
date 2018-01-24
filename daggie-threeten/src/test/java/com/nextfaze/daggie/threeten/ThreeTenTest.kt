@file:Suppress("IllegalIdentifier")

package com.nextfaze.daggie.threeten

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.threeten.bp.ZoneId
import java.util.*

@Config(application = RobolectricApplication::class)
@RunWith(RobolectricTestRunner::class)
class ThreeTenTest {

    private val context = RuntimeEnvironment.application

    @Test fun `zone id emits initial device zone id`() {
        setTimeZone("GMT+0:00")
        val current = ZoneId.systemDefault()
        val observer = zoneId().test().assertNotTerminated()
        assertThat(observer.values().first()).isEqualTo(current)
    }

    @Test fun `zone id emits new device zone id after timezone changed`() {
        setTimeZone("GMT+0:00")
        val observer = zoneId().test().assertNotTerminated()
        setTimeZone("GMT+1:00")
        assertThat(observer.values().size).isEqualTo(2)
    }

    private fun zoneId() = ThreeTenModule().zoneIdFlowable(context)

    private fun setTimeZone(id: String) {
        TimeZone.setDefault(TimeZone.getTimeZone(id))
        context.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED))
    }

    @Test fun `locale emits initial locale`() {
        setLocale(Locale.GERMANY)
        val observer = locale().test().assertNotTerminated()
        assertThat(observer.values().first()).isEqualTo(Locale.GERMANY)
    }

    @Test fun `locale emits new device locale after locale changed`() {
        setLocale(Locale.CHINA)
        val observer = locale().test().assertNotTerminated()
        setLocale(Locale.CANADA)
        assertThat(observer.values().size).isEqualTo(2)
    }

    private fun locale() = ThreeTenModule().localeFlowable(context)

    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        context.sendBroadcast(Intent(Intent.ACTION_LOCALE_CHANGED))
    }
}
