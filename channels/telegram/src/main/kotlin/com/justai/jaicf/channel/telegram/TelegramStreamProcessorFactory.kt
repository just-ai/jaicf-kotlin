package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.justai.jaicf.channel.telegram.streaming.TelegramStreamProcessor
import kotlinx.coroutines.CoroutineDispatcher

fun interface TelegramStreamProcessorFactory {
    fun create(
        api: Bot,
        chatId: ChatId,
        debounceMs: Long,
        dispatcher: CoroutineDispatcher,
        parseMode: ParseMode?
    ): TelegramStreamProcessor
}