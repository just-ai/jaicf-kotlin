package com.justai.jaicf.channel.jaicp

/**
 * Events which can be send with JaicpBotRequest
 *
 * @property FILE_EVENT - is sent when user sends file or image. File will be uploaded to s3 storage,
 *  link will be provided in request.telephony?.jaicp?.data
 * @property LIVE_CHAT_FINISHED - is sent when livechat is finished and request execution is returned to bot.
 * @property NO_LIVE_CHAT_OPERATORS_ONLINE - is sent when scenario attempted switchToLiveChat, but no operators were online.
 *
 * @see com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
 * @see com.justai.jaicf.channel.jaicp.reactions.switchToLiveChat
 * */
@Suppress("MemberVisibilityCanBePrivate")
object JaicpEvents {
    const val FILE_EVENT = "fileEvent"
    const val LIVE_CHAT_FINISHED = "livechatFinished"
    const val NO_LIVE_CHAT_OPERATORS_ONLINE = "noLivechatOperatorsOnline"
}