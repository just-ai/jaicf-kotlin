package com.justai.jaicf.helpers.kotlin

internal fun <T> Boolean.ifTrue(function: () -> T?): T? = if (this) function.invoke() else null