package com.justai.jaicf.channel.slack

import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
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

@Suppress("MemberVisibilityCanBePrivate")
class SlackReactions(
    val context: Context
) : Reactions(), JaicpCompatibleAsyncReactions {

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

    override fun say(text: String): SayReaction {
        when (context) {
            is SayUtility -> context.say(text)
            is ActionRespondUtility -> context.respond(text)
        }
        return SayReaction.create(text)
    }

    override fun image(url: String) = image(
            ImageBlock.builder()
                .imageUrl(url)
                .altText(url)
                .build()
        )


    fun image(image: ImageBlock): ImageReaction {
        respond(listOf(image))
        return ImageReaction.create(image.imageUrl)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(*buttons.map { it to it }.toTypedArray())
    }

    fun buttons(vararg buttons: Pair<String, String>): ButtonsReaction {
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

        return ButtonsReaction.create(buttons.map { it.first })
    }
}
