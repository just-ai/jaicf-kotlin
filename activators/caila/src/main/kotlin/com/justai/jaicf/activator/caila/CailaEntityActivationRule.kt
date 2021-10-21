package com.justai.jaicf.activator.caila

import com.justai.jaicf.builder.ActivationRulesBuilder
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.activation.ActivationRuleAdapter

open class CailaEntityActivationRule(val matches: (CailaEntityActivatorContext) -> Boolean) : ActivationRuleAdapter()
open class CailaEntityByNameActivationRule(val entity: String) : CailaEntityActivationRule({ it.entity == entity })

fun ActivationRulesBuilder.cailaEntity(entity: String) = rule(CailaEntityByNameActivationRule(entity))