package com.justai.jaicf.activator.llm.function

import com.fasterxml.jackson.annotation.JsonInclude

typealias LLMFunctionParametersBuilder = LLMFunction.Parameters.Builder.() -> Unit

data class LLMFunction(
    val name: String,
    val description: String,
    val parameters: Parameters
) {
    data class Parameters(
        val type: String = "object",
        val required: List<String> = emptyList(),
        val properties: Map<String, Property>
    ) {
        class Builder {
            private val properties = mutableMapOf<String, Property>()
            var required = arrayOf<String>()

            infix fun String.to(property: Property) =
                properties.put(this, property)

            infix fun String.to(description: String) =
                properties.put(this, string(description))

            internal fun build() = Parameters(
                required = required.toList(),
                properties = properties.toMap()
            )

            fun string(description: String = "", enum: List<String> = emptyList()) =
                Property.StringProperty(description, enum)
            fun number(description: String = "") =
                Property(Property.Type.number, description)
            fun boolean(description: String = "") =
                Property(Property.Type.boolean, description)
            fun array(description: String = "", items: Property) =
                Property.ArrayProperty(description, items)
            fun obj(description: String = "", builder: LLMFunctionParametersBuilder) =
                Builder().apply(builder).build().let {
                    Property.ObjectProperty(description, it.required, it.properties)
                }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    open class Property(
        val type: Type,
        open val description: String,
    ) {
        enum class Type {
            string, number, boolean, array, `object`
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class StringProperty(
            override val description: String = "",
            val enum: List<String>? = null,
        ) : Property(Type.string, description)

        data class ArrayProperty(
            override val description: String = "",
            val items: Property,
        ) : Property(Type.array, description)

        data class ObjectProperty(
            override val description: String = "",
            val required: List<String> = emptyList(),
            val properties: Map<String, Property>
        ) : Property(Type.`object`, description)
    }

    companion object {
        fun create(name: String, description: String, builder: LLMFunctionParametersBuilder) =
            LLMFunction(name, description, Parameters.Builder().apply(builder).build())
    }
}