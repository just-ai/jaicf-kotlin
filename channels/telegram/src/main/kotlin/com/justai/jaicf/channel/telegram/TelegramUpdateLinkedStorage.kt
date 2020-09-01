package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.Update
import com.justai.jaicf.channel.http.HttpBotRequest

internal object TelegramUpdateLinkedStorage {
    private const val CAPACITY = 5
    private val storage: MutableMap<Int, HttpBotRequest> = mutableMapOf()

    fun take(hash: Int) = storage[hash]!!
    fun put(hash: Int, req: HttpBotRequest) {
        synchronized(this) {
            if (storage.size == CAPACITY) {
                storage.remove(storage.keys.first())
            }
            storage[hash] = req
        }
    }
}

internal var Update.httpBotRequest: HttpBotRequest
    get() = TelegramUpdateLinkedStorage.take(hashCode())
    set(req) = TelegramUpdateLinkedStorage.put(hashCode(), req)
