package com.justai.jaicf.channel.jaicp.channels

/**
 * Events which can be send with JaicpBotRequest in Telephony channel
 *
 * SPEECH_NOT_RECOGNISED    - is sent when ASR provider could not detect speech.
 * HANGUP                   - is sent when client hanged up.
 * BOT_HANGUP               - is sent when bot hanged up.
 * RINGING                  - is sent when we have received incoming call or started outgoing call, but client hadn't yet answered.
 * NO_DTMF_ANSWER           - is sent when we expect client to dial specific code on keypad, but client dialed nothing.
 *
 * @see com.justai.jaicf.channel.jaicp.JaicpEvents
 * */
object TelephonyEvents {
    const val SPEECH_NOT_RECOGNISED = "speechNotRecognized"
    const val BOT_HANGUP = "botHangup"
    const val HANGUP = "hangup"
    const val RINGING = "ringing"
    const val NO_DTMF_ANSWER = "noDtmfAnswerEvent"
    const val BARGE_IN_EVENT = "bargeInIntent"
}