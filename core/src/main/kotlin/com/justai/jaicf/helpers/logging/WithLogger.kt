package com.justai.jaicf.helpers.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface WithLogger {
    val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
}