package com.justai.jaicf.helpers.kotlin

import kotlinx.coroutines.CoroutineDispatcher

interface WithDispatcher {
    val requestDispatcher: CoroutineDispatcher
}