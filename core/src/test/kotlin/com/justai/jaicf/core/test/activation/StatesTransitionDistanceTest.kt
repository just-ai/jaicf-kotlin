package com.justai.jaicf.core.test.activation

import com.justai.jaicf.core.test.BaseTest
import com.justai.jaicf.model.state.StatesTransition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StatesTransitionDistanceTest : BaseTest() {

    @Test
    fun `should calculate distance from root`() {
        fun fromRootDistance(state: String) = StatesTransition("/", state).distance

        step("Check distance from root to top level state")
        assertEquals(0, fromRootDistance("/child"))

        step("Check distance from root to indirect child")
        assertEquals(2, fromRootDistance("/some/state"))
    }

    @Test
    fun `should calculate distance to root`() {
        fun toRootDistance(state: String) = StatesTransition(state, "/").distance

        step("Check distance of transition to root")
        assertEquals(2, toRootDistance("/some/state"))

        step("Check distance of transition to root")
        assertEquals(1, toRootDistance("/child"))
    }

    @Test
    fun `should calculate distance between two states`() {
        fun distance(from: String, to: String) = StatesTransition(from, to).distance

        step("Check distance to child state. Child states and transitions by fromState should have least distance.")
        assertEquals(0, distance("/some/state", "/some/state/child"))
        assertEquals(0, distance("/some/state", "/some/state/child/by/from/state/modifier"))

        step("Check distance to same state or sibling. Same state transitions should have distance as siblings.")
        assertEquals(1, distance("/some/state", "/some/state"))
        assertEquals(1, distance("/some/state", "/some/sibling"))

        step("Check distance between states")
        assertEquals(3, distance("/some/current/state", "/some/from/state/transition"))
        assertEquals(3, distance("/some/current/nested/state", "/some/transition"))
    }
}