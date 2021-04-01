package com.justai.jaicf.helpers.logging

import com.justai.jaicf.context.DefaultActionContext
import org.slf4j.LoggerFactory

val DefaultActionContext.logger
    get() = LoggerFactory.getLogger(this.context.dialogContext.currentState)

fun DefaultActionContext.log(msg: String) = logger.info(msg)
