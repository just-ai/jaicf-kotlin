package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import com.justai.jaicf.channel.telegram.SuccessfulPaymentHandler.*

internal fun Dispatcher.successfulPayment(body: SuccessfulPaymentHandlerEnvironment.() -> Unit) {
    addHandler(SuccessfulPaymentHandler(body))
}

internal class SuccessfulPaymentHandler(
    private val handlePreCheckoutQuery: SuccessfulPaymentHandlerEnvironment.() -> Unit
) : Handler {

    data class SuccessfulPaymentHandlerEnvironment(
        val bot: Bot,
        val update: Update,
        val message: Message,
        val successfulPayment: SuccessfulPayment
    )

    override fun checkUpdate(update: Update) = update.message?.successfulPayment != null

    override suspend fun handleUpdate(
        bot: Bot,
        update: Update
    ) {
        val preCheckoutQueryHandlerEnv = SuccessfulPaymentHandlerEnvironment(
            bot,
            update,
            checkNotNull(update.message),
            checkNotNull(update.message?.successfulPayment)
        )
        handlePreCheckoutQuery(preCheckoutQueryHandlerEnv)
    }
}
