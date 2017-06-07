package com.nextfaze.daggie

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OrderedTest {
    @Test fun naturalOrderIsOrderIntegerAscending() {
        assertThat(listOf(
                Ordered(0, "a"),
                Ordered(2, "b"),
                Ordered(1, "c")
        ).sorted()).containsExactly(
                Ordered(0, "a"),
                Ordered(1, "c"),
                Ordered(2, "b")
        )
    }
}