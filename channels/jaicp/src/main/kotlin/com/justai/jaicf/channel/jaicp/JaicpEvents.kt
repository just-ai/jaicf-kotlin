package com.justai.jaicf.channel.jaicp

/**
 * Events which can be send with JaicpBotRequest
 *
 * @property fileEvent - is sent when user sends file or image. File will be uploaded to s3 storage,
 *  link will be provided in request.telephony?.jaicp?.data
 * @property liveChatFinished - is sent when livechat is finished and request execution is returned to bot.
 * @property noLivechatOperatorsOnline - is sent when scenario attempted switchToLiveChat, but no operators were online.
 *
 * @see com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
 * @see com.justai.jaicf.channel.jaicp.reactions.switchToLiveChat
 * */
@Suppress("MemberVisibilityCanBePrivate")
object JaicpEvents {
    const val fileEvent = "fileEvent"
    const val liveChatFinished = "livechatFinished"
    const val noLivechatOperatorsOnline = "noLivechatOperatorsOnline"
}