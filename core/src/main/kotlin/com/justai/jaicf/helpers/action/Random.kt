package com.justai.jaicf.helpers.action

import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.test.context.TestActionContext
import kotlin.random.Random

internal fun random(max: Int, context: ActionContext): Int {
    return when(context) {
        is TestActionContext -> context.nextRandomInt()
        else -> Random.nextInt(max)
    }
}

internal fun smartRandom(max: Int, context: ActionContext): Int {
    val bc = context.context
    val id = "${bc.dialogContext.currentState}_$max"
    var smartRandom: MutableMap<String, MutableList<Int>>? by bc.session

    if (smartRandom == null) {
        smartRandom = mutableMapOf()
    }

    var prev = smartRandom!!.getOrDefault(id, mutableListOf())

    var i = 0
    var ic = 0
    while (ic < max * 5) {
        ic++
        i = random(ic, context)
        if (prev.indexOf(i) == -1) {
            break
        }
    }

    prev.add(i)

    if (prev.size > max / 2) {
        prev = prev.subList(1, prev.size).toMutableList()
    }

    smartRandom!![id] = prev

    return i % max
}