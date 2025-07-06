package com.justai.jaicf.activator.llm.tool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
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

    fun str(
        name: String,
        description: String? = null,
        required: Boolean = false,
        values: List<String>? = null
    ) {
        val node = mapper.createObjectNode()
        node.put("type", "string")
        description?.let { node.put("description", it) }
        values?.let { node.set<ArrayNode>("enum", mapper.valueToTree(values)) }
        properties[name] = JsonValue.fromJsonNode(node)
        if (required) requiredProps.add(name)
    }

    fun num(
        name: String,
        description: String? = null,
        required: Boolean = false
    ) {
        val node = mapper.createObjectNode()
        node.put("type", "number")
        description?.let { node.put("description", it) }
        properties[name] = JsonValue.fromJsonNode(node)
        if (required) requiredProps.add(name)
    }

    fun bool(
        name: String,
        description: String? = null,
        required: Boolean = false
    ) {
        val node = mapper.createObjectNode()
        node.put("type", "boolean")
        description?.let { node.put("description", it) }
        properties[name] = JsonValue.fromJsonNode(node)
        if (required) requiredProps.add(name)
    }

    fun obj(
        name: String,
        description: String? = null,
        required: Boolean = false,
        builder: JsonSchemaBuilder.() -> Unit
    ) {
        val subBuilder = JsonSchemaBuilder()
        subBuilder.builder()
        val node = mutableMapOf<String, JsonValue>()
        node["type"] = JsonValue.from("object")
        node["properties"] = JsonValue.from(subBuilder.properties)
        if (subBuilder.requiredProps.isNotEmpty())
            node["required"] = JsonValue.from(subBuilder.requiredProps)
        description?.also { node["description"] = JsonValue.from(it) }
        properties[name] = JsonValue.from(node)
        if (required) requiredProps.add(name)
    }

    fun arr(
        name: String,
        itemsType: JsonType,
        required: Boolean = false,
        description: String? = null
    ) {
        val map = mutableMapOf<String, JsonValue>()
        map["type"] = JsonValue.from("array")
        description?.let { map["description"] = JsonValue.from(it) }
        map["items"] = JsonValue.from(mapOf("type" to JsonValue.from(itemsType.name)))
        properties[name] = JsonValue.from(map)
        if (required) requiredProps.add(name)
    }

    fun arr(
        name: String,
        required: Boolean = false,
        description: String? = null,
        itemsBuilder: JsonSchemaBuilder.() -> Unit
    ) {
        val subBuilder = JsonSchemaBuilder()
        subBuilder.itemsBuilder()
        val objMap = mutableMapOf<String, JsonValue>()
        objMap["type"] = JsonValue.from("object")
        objMap["properties"] = JsonValue.from(subBuilder.properties)
        if (subBuilder.requiredProps.isNotEmpty())
            objMap["required"] = JsonValue.from(subBuilder.requiredProps)
        val map = mutableMapOf<String, JsonValue>()
        map["type"] = JsonValue.from("array")
        description?.let { map["description"] = JsonValue.from(it) }
        map["items"] = JsonValue.from(objMap)
        properties[name] = JsonValue.from(map)
        if (required) requiredProps.add(name)
    }

    fun build() = mutableMapOf<String, JsonValue>().apply {
        put("type", JsonValue.from("object"))
        put("properties", JsonValue.from(properties))
        put("additionalProperties", JsonValue.from(false))
        if (requiredProps.isNotEmpty()) {
           put("required", JsonValue.from(requiredProps))
        }
    }.toMap()
}
