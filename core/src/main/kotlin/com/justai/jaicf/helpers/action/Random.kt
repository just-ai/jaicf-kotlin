package com.justai.jaicf.helpers.action

import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.test.context.TestActionContext
import kotlin.random.Random

internal fun random(max: Int, context: DefaultActionContext): Int {
    return when(context) {
        is TestActionContext -> context.nextRandomInt()
        else -> Random.nextInt(max)
    }
}

internal fun smartRandom(max: Int, context: DefaultActionContext): Int {
    val bc = context.context
    val id = "${bc.dialogContext.currentState}_$max"
    var smartRandom: MutableMap<String, MutableList<Int>>? by bc.session

    if (smartRandom == null) {
        smartRandom = mutableMapOf()
    }

    val prev = smartRandom!!.computeIfAbsent(id) { mutableListOf() }
    val randoms = generateSequence { random(max, context) }.take(max * 5)

    val rand = randoms.firstOrNull { it !in prev } ?: 0
    prev.add(rand)

    if (prev.size > max / 2) {
        prev.removeFirst()
    }

    return rand
}