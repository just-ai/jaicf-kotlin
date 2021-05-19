package com.justai.jaicf.examples.viber

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.viber.api.CarouselElement
import com.justai.jaicf.channel.viber.api.ViberEvent
import com.justai.jaicf.channel.viber.api.isReceivedAsSilent
import com.justai.jaicf.channel.viber.contact
import com.justai.jaicf.channel.viber.conversationStarted
import com.justai.jaicf.channel.viber.delivered
import com.justai.jaicf.channel.viber.file
import com.justai.jaicf.channel.viber.location
import com.justai.jaicf.channel.viber.image
import com.justai.jaicf.channel.viber.sdk.api.KeyboardBuilder
import com.justai.jaicf.channel.viber.sdk.api.OpenMapButton
import com.justai.jaicf.channel.viber.sdk.api.RedirectButton
import com.justai.jaicf.channel.viber.sdk.api.ReplyButton
import com.justai.jaicf.channel.viber.sdk.api.ViberButton
import com.justai.jaicf.channel.viber.sdk.api.toKeyboard
import com.justai.jaicf.channel.viber.sdk.message.Location
import com.justai.jaicf.channel.viber.sdk.message.PictureMessage
import com.justai.jaicf.channel.viber.seen
import com.justai.jaicf.channel.viber.sticker
import com.justai.jaicf.channel.viber.subscribed
import com.justai.jaicf.channel.viber.text
import com.justai.jaicf.channel.viber.viber
import com.justai.jaicf.channel.viber.video
import java.io.File

val ViberTestScenario = Scenario {
    state("new_conversation") {
        activators {
            event(ViberEvent.CONVERSATION_STARTED)
        }

        action(viber.conversationStarted) {
            reactions.say("Greetings")
        }
    }

    state("subscribed") {
        activators {
            event(ViberEvent.SUBSCRIBED)
        }

        action(viber.subscribed) {
            reactions.say("Thank you for using us")
            reactions.image("https://i.ytimg.com/vi/8W2njNW6hI0/hqdefault.jpg")
        }
    }

    state("seen", noContext = true) {
        activators {
            event(ViberEvent.SEEN)
        }

        action(viber.seen) {
            // You can do something
        }
    }

    state("delivered", noContext = true) {
        activators {
            event(ViberEvent.DELIVERED)
        }

        action(viber.delivered) {
            // You can do something
        }
    }

    state("file") {
        activators {
            event(ViberEvent.FILE_MESSAGE)
        }

        action(viber.file) {
            reactions.say("You sent me a file")
            val fileMessage = request.message
            reactions.file(fileMessage.url, File(fileMessage.filename))
        }
    }

    state("image") {
        activators {
            event(ViberEvent.IMAGE_MESSAGE)
        }

        action(viber.image) {
            reactions.say("You sent me a picture")
            reactions.image(request.message.url)
        }
    }

    state("video") {
        activators {
            event(ViberEvent.VIDEO_MESSAGE)
        }

        action(viber.video) {
            reactions.say("You sent me a video")
            reactions.video(request.message.url)
        }
    }

    state("location") {
        activators {
            event(ViberEvent.LOCATION_MESSAGE)
        }

        action(viber.location) {
            reactions.say("You sent me a location")
            val (latitude, longitude) = request.message.location
            reactions.location(latitude, longitude)
        }
    }

    state("sticker") {
        activators {
            event(ViberEvent.STICKER_MESSAGE)
        }

        action(viber.sticker) {
            reactions.say("You sent me a sticker")
            reactions.sticker(request.message.stickerId)
        }
    }

    state("contact") {
        activators {
            event(ViberEvent.CONTACT_MESSAGE)
        }

        action(viber.contact) {
            reactions.say("You sent me a contact of ${request.message.contact.name}")
        }
    }

    state("commands") {
        activators {
            regex("commands")
        }

        action(viber) {
            reactions.keyboard {
                row("Timer", "Message with keyboard")
                reply("Inline buttons")
                reply("Inline buttons without message")
                row {
                    reply("Map")
                    reply("Carousel")
                }
                redirect("google", "google.com")
            }
        }

        state("timer") {
            activators {
                regex("Timer")
            }

            action(viber.text) {
                for (i in 5 downTo 1) {
                    reactions.say(i)
                    Thread.sleep(1000)
                }
                reactions.say("Timer is over")
            }
        }

        state("message with keyboard") {
            activators {
                regex("Message with keyboard")
            }

            action(viber.text) {
                val viberKeyboard = KeyboardBuilder(ViberButton.Style(backgroundColor = "#fdebd0")).apply {
                    row("1", "2", "3")
                    row("4")
                }.build()
                reactions.sendMessage(
                    PictureMessage(
                        "https://sun9-62.userapi.com/srBmxyK8e5SdZ2Yk2ZbqlNSk-966AC3mvMJJZA/xou0d6fBUO0.jpg",
                        keyboard = viberKeyboard.toKeyboard()
                    )
                )
            }
        }

        state("inline_buttons") {
            activators {
                regex("Inline buttons")
            }

            action(viber.text) {
                val viberKeyboard = KeyboardBuilder(ViberButton.Style(backgroundColor = "#fdebd0")).apply {
                    row("1", "2", "3")
                    row("4")
                }.build()
                reactions.image("https://sun9-62.userapi.com/srBmxyK8e5SdZ2Yk2ZbqlNSk-966AC3mvMJJZA/xou0d6fBUO0.jpg")
                reactions.inlineButtons(viberKeyboard = viberKeyboard)
            }
        }

        state("inline_buttons_without_message") {
            activators {
                regex("Inline buttons without message")
            }

            action(viber.text) {
                reactions.inlineButtons(ViberButton.Style(backgroundColor = "#fdebd0")) {
                    row("1", "2", "3")
                    row("4")
                }
            }
        }
        state("open_map") {
            activators {
                regex("Map")
            }

            action(viber.text) {
                reactions.inlineButtons(ViberButton.Style(backgroundColor = "#fdebd0")) {
                    row {
                        button(OpenMapButton("Open map", location = Location(30.0, 60.0)))
                    }
                }
            }
        }

        state("carousel") {
            activators {
                regex("Carousel")
            }

            action(viber.text) {
                reactions.say("Description for carousel element")
                reactions.carousel(
                    CarouselElement(
                        "https://p1.zoon.ru/preview/nT8_s6xSaDEyjaBARdqrgw/504x270x85/1/4/b/original_52568bef40c08891208bc999_5c0a2f76d23b1.jpg",
                        "First mall title",
                        "Subtitle for first mall",
                        ReplyButton("Reply button")
                    ),
                    CarouselElement(
                        "https://static.tildacdn.com/tild3262-6461-4161-b339-643136336265/7393505506535bbfc4e5.jpg",
                        "Title of second shop"
                    ),
                    CarouselElement(
                        "https://40.img.avito.st/640x480/9202090740.jpg",
                        "Third title",
                        button = RedirectButton("Redirect button", redirectUrl = "https://5ka.ru/?100try.com")
                    )
                )
            }
        }
    }

    fallback {
        viber.text {
            if (request.isReceivedAsSilent) {
                return@text
            }

            reactions.say("You can send file, image, video, location, sticker, contact, text or test commands. To test commands write 'commands'")
            viber.text {
                reactions.say("You write ${request.message.text}")
            }
        }
    }
}

val viberTestBot = BotEngine(
    scenario = ViberTestScenario,
    activators = arrayOf(
        RegexActivator
    )
)

