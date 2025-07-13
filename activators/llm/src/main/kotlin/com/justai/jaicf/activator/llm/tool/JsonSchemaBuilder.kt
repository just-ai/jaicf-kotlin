package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.openai.core.JsonValue

enum class JsonType {
    string,
    number,
    integer,
    boolean,
}

class JsonSchemaBuilder {
    private val mapper = ObjectMapper()
    private val properties = mutableMapOf<String, JsonValue>()
    private val requiredProps = mutableListOf<String>()

    private fun addProperty(
        name: String,
        type: JsonType,
        description: String? = null,
        required: Boolean = false,
        extras: (ObjectNode.() -> Unit)? = null
    ) {
        val node = mapper.createObjectNode().apply {
            put("type", type.name)
            description?.let { put("description", it) }
            extras?.invoke(this)
        }
        properties[name] = JsonValue.fromJsonNode(node)
        if (required) requiredProps.add(name)
    }

    fun str(name: String, description: String? = null, required: Boolean = false, values: List<String>? = null) =
        addProperty(name, JsonType.string, description, required) {
            values?.let { set<ArrayNode>("enum", mapper.valueToTree(it)) }
        }

    fun num(name: String, description: String? = null, required: Boolean = false) =
        addProperty(name, JsonType.number, description, required)

    fun int(name: String, description: String? = null, required: Boolean = false) =
        addProperty(name, JsonType.integer, description, required)

    fun bool(name: String, description: String? = null, required: Boolean = false) =
        addProperty(name, JsonType.boolean, description, required)

    fun obj(name: String, description: String? = null, required: Boolean = false, builder: JsonSchemaBuilder.() -> Unit) {
        val sub = JsonSchemaBuilder().apply(builder)
        val node = mutableMapOf(
            "type" to JsonValue.from("object"),
            "properties" to JsonValue.from(sub.properties)
        ).apply {
            if (sub.requiredProps.isNotEmpty()) put("required", JsonValue.from(sub.requiredProps))
            description?.let { put("description", JsonValue.from(it)) }
        }
        properties[name] = JsonValue.from(node)
        if (required) requiredProps.add(name)
    }

    fun arr(name: String, description: String? = null, itemsType: JsonType, required: Boolean = false) {
        val map = mutableMapOf(
            "type" to JsonValue.from("array"),
            "items" to JsonValue.from(mapOf("type" to JsonValue.from(itemsType.name)))
        )
        description?.let { map["description"] = JsonValue.from(it) }
        properties[name] = JsonValue.from(map)
        if (required) requiredProps.add(name)
    }

    fun arr(name: String, description: String? = null, required: Boolean = false, itemsBuilder: JsonSchemaBuilder.() -> Unit) {
        val sub = JsonSchemaBuilder().apply(itemsBuilder)
        val objMap = mutableMapOf(
            "type" to JsonValue.from("object"),
            "properties" to JsonValue.from(sub.properties)
        ).apply {
            if (sub.requiredProps.isNotEmpty()) put("required", JsonValue.from(sub.requiredProps))
        }
        val map = mutableMapOf(
            "type" to JsonValue.from("array"),
            "items" to JsonValue.from(objMap)
        )
        description?.let { map["description"] = JsonValue.from(it) }
        properties[name] = JsonValue.from(map)
        if (required) requiredProps.add(name)
    }

    fun build() = mutableMapOf(
        "type" to JsonValue.from("object"),
        "properties" to JsonValue.from(properties),
        "additionalProperties" to JsonValue.from(false)
    ).apply {
        if (requiredProps.isNotEmpty()) {
            put("required", JsonValue.from(requiredProps))
        }
    }.toMap()
}
