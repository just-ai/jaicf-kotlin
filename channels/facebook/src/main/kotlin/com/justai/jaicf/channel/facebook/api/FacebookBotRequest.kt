package com.justai.jaicf.channel.facebook.api

import com.github.messenger4j.webhook.Event
import com.github.messenger4j.webhook.event.*
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest

val BotRequest.facebook
    get() = this as? FacebookBotRequest

interface FacebookBotRequest: BotRequest {
    val event: BaseEvent
}

data class FacebookTextBotRequest(
    override val event: TextMessageEvent
): FacebookBotRequest, QueryBotRequest(
    clientId = event.senderId(),
    input = event.text()
)

data class FacebookQuickReplyBotRequest(
    override val event: QuickReplyMessageEvent
): FacebookBotRequest, QueryBotRequest(
    clientId = event.senderId(),
    input = event.text()
)

sealed class FacebookEventBotRequest(
    override val event: BaseEvent,
    type: String
): FacebookBotRequest, EventBotRequest(
    clientId = event.senderId(),
    input = type
)

data class FacebookOptInBotRequest(
    override val event: OptInEvent
): FacebookEventBotRequest(event, FacebookEvent.OPT_IN)

data class FacebookAccountLinkingBotRequest(
    override val event: AccountLinkingEvent
): FacebookEventBotRequest(event, FacebookEvent.ACCOUNT_LINKING)

data class FacebookAttachmentBotRequest(
    override val event: AttachmentMessageEvent
): FacebookEventBotRequest(event, FacebookEvent.ATTACHMENT)

data class FacebookFallbackBotRequest(
    override val event: FallbackEvent
): FacebookEventBotRequest(event, FacebookEvent.FALLBACK)

data class FacebookInstantGameBotRequest(
    override val event: InstantGameEvent
): FacebookEventBotRequest(event, FacebookEvent.INSTANT_GAME)

data class FacebookMessageDeliveredBotRequest(
    override val event: MessageDeliveredEvent
): FacebookEventBotRequest(event, FacebookEvent.MESSAGE_DELIVERED)

data class FacebookMessageEchoBotRequest(
    override val event: MessageEchoEvent
): FacebookEventBotRequest(event, FacebookEvent.MESSAGE_ECHO)

data class FacebookMessageReadBotRequest(
    override val event: MessageReadEvent
): FacebookEventBotRequest(event, FacebookEvent.MESSAGE_READ)

data class FacebookPostBackBotRequest(
    override val event: PostbackEvent
): FacebookEventBotRequest(event, FacebookEvent.POST_BACK)

data class FacebookReferralBotRequest(
    override val event: ReferralEvent
): FacebookEventBotRequest(event, FacebookEvent.REFERRAL)


internal fun Event.toBotRequest(): FacebookBotRequest = when {
    isAccountLinkingEvent -> FacebookAccountLinkingBotRequest(asAccountLinkingEvent())
    isAttachmentMessageEvent -> FacebookAttachmentBotRequest(asAttachmentMessageEvent())
    isInstantGameEvent -> FacebookInstantGameBotRequest(asInstantGameEvent())
    isMessageDeliveredEvent -> FacebookMessageDeliveredBotRequest(asMessageDeliveredEvent())
    isMessageEchoEvent -> FacebookMessageEchoBotRequest(asMessageEchoEvent())
    isMessageReadEvent -> FacebookMessageReadBotRequest(asMessageReadEvent())
    isOptInEvent -> FacebookOptInBotRequest(asOptInEvent())
    isPostbackEvent -> FacebookPostBackBotRequest(asPostbackEvent())
    isQuickReplyMessageEvent -> FacebookQuickReplyBotRequest(asQuickReplyMessageEvent())
    isReferralEvent -> FacebookReferralBotRequest(asReferralEvent())
    isTextMessageEvent -> FacebookTextBotRequest(asTextMessageEvent())
    else -> FacebookFallbackBotRequest(FallbackEvent(senderId(), recipientId(), timestamp()))
}