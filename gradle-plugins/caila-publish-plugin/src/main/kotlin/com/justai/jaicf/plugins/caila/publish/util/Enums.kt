package com.justai.jaicf.plugins.caila.publish.util

/**
 * Тип выполняемой задачи
 */
enum class TaskType(val tag: String) {
    TEXT2VEC("text2vec"),
    TEXT_CLASSIFICATION("text-classification"),
    NER("ner"),
    TEXT_PROCESSING("text-processing"),
    CHAT_COMPLETION("chat-completion"),
    ASR("asr"),
    TTS("tts"),
    CHAT("chat"),
    IMAGE_GENERATION("image-generation"),
    CUSTOM("custom"),
    FILE_BASE64("file_base64"),
    MCP_SERVER("mcp_server")
}

/**
 * Возможные варианты хостинга
 */
enum class HostingType(val type: String) {
    EXTERNAL("EXTERNAL"),
    INTERNAL("INTERNAL"),
    AUTOMATIC("AUTOMATIC"),
    HOSTING_SERVER("HOSTING_SERVER")
}

enum class AccessMode(val mode: String) {
    PRIVATE("private"),
    PUBLIC("public"),
    RESTRICTED("restricted"),
}

/**
 * Model type for the service
 */
enum class ModelType(val type: String) {
    WEB_APPLICATION("WEB_APPLICATION"),
    MLP("MLP"),
    MCP_SERVER("MCP_SERVER")
}