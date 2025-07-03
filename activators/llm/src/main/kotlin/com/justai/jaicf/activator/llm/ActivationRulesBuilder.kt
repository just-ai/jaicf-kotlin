package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.builder.ActivationRulesBuilder

class LLMActivationRule(val props: LLMPropsBuilder = DefaultLLMProps) : EventByNameActivationRule(LLMEvent.RESPONSE)

fun ActivationRulesBuilder.llmActivator(props: LLMPropsBuilder = DefaultLLMProps) =
    rule(LLMActivationRule(props))
