package com.nextfaze.daggie.permissions.testing

import android.Manifest.permission.ACCESS_FINE_LOCATION
import com.nextfaze.daggie.permissions.PermissionState.DENIED
import com.nextfaze.daggie.permissions.PermissionState.GRANTED
import org.junit.Test

class FakePermissionsTest {

    private val permissions = FakePermissions()

    @Test fun `when state map is changed, subscribers get updated state`() {
        permissions.state(ACCESS_FINE_LOCATION).test().apply {
            permissions.states = mapOf(ACCESS_FINE_LOCATION to GRANTED)
            assertValues(DENIED, GRANTED)
        }
    }
}
