package com.justai.jaicf.channel.yandexalice.api.model

typealias IntentName = String

object AliceIntent {
    const val CONFIRM: IntentName = "YANDEX.CONFIRM"
    const val REJECT: IntentName = "YANDEX.REJECT"
    const val HELP: IntentName = "YANDEX.HELP"
    const val REPEAT: IntentName = "YANDEX.REPEAT"
}
