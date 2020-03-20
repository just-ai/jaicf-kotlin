package com.justai.jaicf.examples.alice

import com.justai.jaicf.channel.yandexalice.AliceEvent
import com.justai.jaicf.channel.yandexalice.alice
import com.justai.jaicf.model.scenario.Scenario

object MainScenario: Scenario() {
    init {
        state("main") {
            activators {
                event(AliceEvent.START)
            }

            action {
                reactions.say("Майор на связи. Докладывайте.")
                reactions.alice?.image(
                    "https://i.imgur.com/YOnWzLM.jpg",
                    "Майор на связи",
                    "Начните сообщение со слова \"Докладываю\"")
            }
        }

        state("report") {
            activators {
                regex("докладываю .+")
            }

            action {
                reactions.run {
                    say("Спасибо.")
                    sayRandom(
                        "Ваш донос зарегистрирован под номером ${random(1000, 9000)}.",
                        "Оставайтесь на месте. Не трогайте вещественные доказательства."
                    )
                    say("У вас есть еще какая-нибудь информация?")
                    buttons("Да", "Нет")
                }
            }

            state("yes") {
                activators {
                    regex("да")
                }

                action {
                    reactions.say("Докладывайте.")
                }
            }

            state("no") {
                activators {
                    regex("нет")
                    regex("отбой")
                }

                action {
                    reactions.sayRandom("Отбой.", "До связи.")
                    reactions.alice?.endSession()
                }
            }
        }

        state("fallback", noContext = true) {
            activators {
                catchAll()
            }

            action {
                reactions.say("Не тратьте моего времени зря. Начните сообщение со слова \"Докладываю\".")
            }
        }
    }
}