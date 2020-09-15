package com.justai.jaicf.helpers.kotlin

import com.github.h0tk3y.kotlinFun.util.WeakIdentityHashMap
import kotlin.reflect.KProperty

class PropertyWithBackingField<R, T : Any>(
    val initializer: (R) -> T = { throw IllegalStateException("Not initialized.") }
) {
    private val map = WeakIdentityHashMap<R, T>()

    operator fun getValue(thisRef: R, property: KProperty<*>): T =
        map[thisRef] ?: setValue(thisRef, property, initializer(thisRef))

    operator fun setValue(thisRef: R, property: KProperty<*>, value: T): T {
        map[thisRef] = value
        return value
    }
}