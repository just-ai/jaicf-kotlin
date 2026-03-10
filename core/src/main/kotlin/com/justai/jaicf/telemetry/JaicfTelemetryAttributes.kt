package com.justai.jaicf.telemetry

/**
 * JAICF telemetry attribute names (keys for span attributes).
 */
object JaicfTelemetryAttributes {
    const val CLIENT_ID = "jaicf.client_id"
    const val CLIENT_ID_ALT = "jaicf.client.id"
    const val REQUEST_TYPE = "jaicf.request.type"
    const val REQUEST_CLIENT_ID = "jaicf.request.client_id"
    const val REQUEST_INPUT = "jaicf.request.input"
    const val REQUEST_RESPONSE = "jaicf.request.response"
    const val SESSION_NEW = "jaicf.session.new"
    const val ACTIVATOR = "jaicf.activator"
    const val ACTIVATOR_NAME = "jaicf.activator.name"
    const val CURRENT_STATE = "jaicf.current_state"
    const val STATE = "jaicf.state"
    const val STATE_PATH = "jaicf.state_path"
    const val STATE_NAME = "jaicf.state.name"
    const val STATE_CURRENT = "jaicf.state.current"
    const val ERROR_TYPE = "jaicf.error.type"
    const val ERROR_MESSAGE = "jaicf.error.message"
    const val ERROR_STATE = "jaicf.error.state"

    /** Span name for bot request span (used for parent lookup). */
    const val BOT_REQUEST_SPAN = "jaicf.bot.request"
}
