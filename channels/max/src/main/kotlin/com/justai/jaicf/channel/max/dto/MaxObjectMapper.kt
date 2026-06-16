package com.justai.jaicf.channel.max.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/** Shared, pre-configured Jackson mapper for parsing incoming Max updates. */
internal val maxObjectMapper: ObjectMapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
