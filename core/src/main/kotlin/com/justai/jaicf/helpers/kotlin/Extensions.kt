package com.justai.jaicf.helpers.kotlin

internal fun <T> Boolean.runIfTrueElseNull(function: () -> T?): T? = if (this) function.invoke() else null