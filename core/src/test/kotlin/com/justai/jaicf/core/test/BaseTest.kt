package com.justai.jaicf.core.test

import com.justai.jaicf.helpers.logging.WithLogger
import org.junit.jupiter.api.BeforeEach

abstract class BaseTest : WithLogger {
    private var step: Int = 1

    @BeforeEach
    fun setUp() {
        step = 1

    }

    fun step(message: String) = println("\nStep: $step: $message\n").also { step++ }
}