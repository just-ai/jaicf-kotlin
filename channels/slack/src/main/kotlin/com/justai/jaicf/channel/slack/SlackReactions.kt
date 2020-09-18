package com.justai.jaicf.channel.slack

import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.logging.SayReaction
import com.slack.api.bolt.context.ActionRespondUtility
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.SayUtility
import com.slack.api.methods.request.users.profile.UsersProfileGetRequest
import com.slack.api.model.User
import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.ImageBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import java.util.*

val Reactions.slack
    get() = this as? SlackReactions

class SlackReactions(
    val context: Context
) : Reactions() {

    val client = context.client()

    private fun nextActionId() = UUID.randomUUID().toString()

    fun getUserProfile(userId: String): User.Profile? {
        return client.usersProfileGet(
            UsersProfileGetRequest.builder()
                .token(context.botToken)
                .user(userId)
                .build()
        ).profile
    }

    fun respond(blocks: List<LayoutBlock>) {
        when (context) {
            is SayUtility -> context.say(blocks)
            is ActionRespondUtility -> context.respond(blocks)
        }
    }

    override fun say(text: String) {
        when (context) {
            is SayUtility -> context.say(text)
            is ActionRespondUtility -> context.respond(text)
        }
        SayReaction.register(text)
    }

    override fun image(url: String) = image(
            ImageBlock.builder()
                .imageUrl(url)
                .altText(url)
                .build()
        )


    fun image(image: ImageBlock) = respond(listOf(image)).also { ImageReaction.register(image.imageUrl) }

    override fun buttons(vararg buttons: String) {
        buttons(*buttons.map { it to it }.toTypedArray())
    }

    fun buttons(vararg buttons: Pair<String, String>) {
        respond(listOf(
            ActionsBlock.builder().elements(
                buttons.map {
                    ButtonElement.builder()
                        .text(PlainTextObject(it.first, false))
                        .value(it.second)
                        .actionId(nextActionId())
                        .build()
                }
            ).build()
        ))

        ButtonsReaction.register(buttons.map { it.first })
    }
}