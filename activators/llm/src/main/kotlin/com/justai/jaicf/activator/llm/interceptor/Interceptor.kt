package com.justai.jaicf.activator.llm.interceptor

import com.justai.jaicf.activator.llm.CallContext

interface Interceptor {
    val configName: String
    fun <T> intercept(chain: Chain<T>): T

    interface Chain<T> {
        val ctx: CallContext

        fun proceed(): T
    }
}