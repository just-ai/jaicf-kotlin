package com.justai.jaicf.helpers.http

fun String.toUrl() = this.replace("(?<=[^:\\s])(/+/)".toRegex(), "/")