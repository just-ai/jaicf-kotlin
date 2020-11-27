package com.justai.jaicf.helpers.kotlin

fun <T> Boolean.ifTrue(function: () -> T?): T? = if (this) function.invoke() else null