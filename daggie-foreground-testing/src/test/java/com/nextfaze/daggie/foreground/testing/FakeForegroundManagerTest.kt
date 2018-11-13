package com.nextfaze.daggie.foreground.testing

import org.junit.Test

class FakeForegroundManagerTest {

    private val foregroundManager = FakeForegroundManager()

    @Test fun `when foreground changed, subscriber should get updated value`() {
        foregroundManager.foreground().test().apply {
            foregroundManager.isForeground = false
            assertValues(true, false)
        }
    }
}
