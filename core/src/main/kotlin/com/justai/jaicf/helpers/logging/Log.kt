package com.justai.jaicf.helpers.logging

import com.justai.jaicf.context.ActionContext
import org.slf4j.LoggerFactory

val ActionContext<*, *, *>.logger
    get() = LoggerFactory.getLogger(this.context.dialogContext.currentState)

fun ActionContext<*, *, *>.log(msg: String) = logger.info(msg)
