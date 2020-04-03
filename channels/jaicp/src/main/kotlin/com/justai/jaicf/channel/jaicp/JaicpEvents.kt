package com.justai.jaicf.channel.jaicp

/**
 * Events which can be send with JaicpBotRequest
 *
 * fileEvent - is sent when user sends file or image.
 * File will be uploaded to s3 storage, link will be provided in request.telephony?.jaicp?.data
 *
 * @see com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
 * */
object JaicpEvents {
    const val fileEvent = "fileEvent"
}