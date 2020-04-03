package com.justai.jaicf.channel.jaicp.channels

/**
 * Events which can be send with JaicpBotRequest in Telephony channel
 *
 * speechNotRecognized  - is sent when ASR provider could not detect speech.
 * hangup               - is sent when client hanged up.
 * ringing              - is sent when we have received incoming call or started outgoing call, but client hadn't yet answered.
 * noDmtfAnswer         - is sent when we expect client to dial specific code on keypad, but client dialed nothing.
 *
 * @see com.justai.jaicf.channel.jaicp.JaicpEvents
 * */
object TelephonyEvents {
    const val speechNotRecognized = "speechNotRecognized"
    const val hangup = "hangup"
    const val ringing = "ringing"
    const val noDtmfAnswer = "noDtmfAnswerEvent"
}