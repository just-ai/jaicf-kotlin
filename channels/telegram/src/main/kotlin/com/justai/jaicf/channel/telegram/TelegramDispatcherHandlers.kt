package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.HandleUpdate
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import com.justai.jaicf.channel.telegram.SuccessfulPaymentHandler.*

internal fun Dispatcher.successfulPayment(body: SuccessfulPaymentHandlerEnvironment.() -> Unit) {
    addHandler(SuccessfulPaymentHandler(body))
}

internal class SuccessfulPaymentHandler(
    handlePreCheckoutQuery: SuccessfulPaymentHandlerEnvironment.() -> Unit
) : Handler(SuccessfulPaymentHandlerProxy(handlePreCheckoutQuery)) {

    override val groupIdentifier = "successfulPayment"

    override fun checkUpdate(update: Update) = update.message?.successfulPayment != null

    data class SuccessfulPaymentHandlerEnvironment(
        val bot: Bot,
        val update: Update,
        val message: Message,
        val successfulPayment: SuccessfulPayment
    )

    private class SuccessfulPaymentHandlerProxy(
        private val handlePreCheckoutQuery: SuccessfulPaymentHandlerEnvironment.() -> Unit
    ) : HandleUpdate {
        override fun invoke(bot: Bot, update: Update) {
            val preCheckoutQueryHandlerEnv = SuccessfulPaymentHandlerEnvironment(
                bot,
                update,
                checkNotNull(update.message),
                checkNotNull(update.message?.successfulPayment)
            )
            handlePreCheckoutQuery(preCheckoutQueryHandlerEnv)
        }
    }
}
