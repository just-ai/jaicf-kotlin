package com.justai.jaicf.test.context

import com.justai.jaicf.context.RequestContext
import java.util.*

/**
 * This [RequestContext] implementation is used during the unit test execution.
 * Adds an ability to pass some arbitrary variables to the scenario and numbers queue for smart random algorithm.
 */
class TestRequestContext(
    override val newSession: Boolean = false
): RequestContext(newSession) {

    val randomNumbers = ArrayDeque<Int>()
    val variables = mutableMapOf<String, Any?>().withDefault { null }
}