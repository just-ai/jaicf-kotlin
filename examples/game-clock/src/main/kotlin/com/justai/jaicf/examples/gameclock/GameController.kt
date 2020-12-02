package com.justai.jaicf.examples.gameclock

import com.justai.jaicf.context.BotContext

class GameController(context: BotContext) {

    var gamers: Int? by context.client
    var colors: MutableList<String> by context.client
    var currentTurn: Int? by context.client
    var currentGamer: Int? by context.client
    var currentTime: Long? by context.client
    var overall: Long? by context.client
    var gamersTime: MutableMap<String, Long> by context.client

    fun nextGamer(): Int? {
        if (gamers == null) return null

        currentTime = null

        currentGamer = when {
            currentGamer == null -> 1
            currentGamer!! >= gamers!! -> 1
            else -> currentGamer!!.inc()
        }

        return currentGamer.also {
            currentTurn = when {
                currentTurn == null -> 1
                currentGamer == 1 -> currentTurn!!.inc()
                else -> currentTurn
            }
        }
    }

    fun prevGamer(): Int? {
        if (gamers == null) return null

        currentTime = null

        currentGamer = when {
            currentGamer == null -> 1
            currentGamer!! > 1 -> currentGamer!!.dec()
            else -> gamers
        }

        return currentGamer.also {
            currentTurn = when {
                currentTurn == null -> 1
                currentGamer == gamers -> currentTurn!!.dec()
                else -> currentTurn
            }
        }
    }

    fun currentColor() = currentGamer?.let {
        if(colors.size > it-1) colors[it-1] else null
    }

    fun currentTurn() = currentTurn ?: 1

    fun currentGamerOverall() = currentColor()?.let { gamersTime[it] ?: 0 } ?: 0

    fun record(timeMs: Long) {
        currentTime = timeMs
        overall = overall?.let { it.plus(timeMs) } ?: timeMs

        currentColor()?.let { color ->
            val time = gamersTime[color] ?: 0
            gamersTime[color] = time.plus(timeMs)
        }
    }

    fun isReady() = gamers != null && !colors.isNullOrEmpty() && colors.size == gamers

    fun reset() {
        restart()
        gamers = null
        colors = mutableListOf()
        gamersTime = mutableMapOf()
    }

    fun restart() {
        currentTurn = null
        currentGamer = null
        currentTime = null
        overall = null
    }
}