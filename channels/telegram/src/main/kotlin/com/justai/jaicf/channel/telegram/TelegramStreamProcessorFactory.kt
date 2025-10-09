package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.justai.jaicf.channel.telegram.streaming.TelegramStreamProcessor
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Factory interface for creating custom TelegramStreamProcessor instances.
 * Implement this interface to provide custom streaming behavior for Telegram messages.
 *
 * Example:
 * ```
 * class CustomStreamProcessorFactory : TelegramStreamProcessorFactory {
 *     override fun create(
 *         api: Bot,
 *         chatId: ChatId,
 *         debounceMs: Long,
 *         dispatcher: CoroutineDispatcher
 *     ): TelegramStreamProcessor {
 *         return CustomTelegramStreamProcessor(api, chatId, debounceMs, dispatcher)
 *     }
 * }
 * ```
 */
fun interface TelegramStreamProcessorFactory {
    /**
     * Creates a new TelegramStreamProcessor instance.
     *
     * @param api the Telegram Bot API instance
     * @param chatId the chat ID where messages will be sent
     * @param debounceMs the debounce delay in milliseconds
     * @param dispatcher the coroutine dispatcher for async operations
     * @return a new TelegramStreamProcessor instance
     */
    fun create(
        api: Bot,
        chatId: ChatId,
        debounceMs: Long,
        dispatcher: CoroutineDispatcher
    ): TelegramStreamProcessor
}