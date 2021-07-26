package com.justai.jaicf.examples.multilingual.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val Jackson = jacksonObjectMapper().apply {
    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
}