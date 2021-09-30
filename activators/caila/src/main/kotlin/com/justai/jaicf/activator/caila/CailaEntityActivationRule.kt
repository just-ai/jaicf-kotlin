package com.justai.jaicf.activator.caila

import com.justai.jaicf.builder.ActivationRulesBuilder
import com.justai.jaicf.model.activation.ActivationRule

open class CailaEntityActivationRule(val matches: (CailaEntityActivatorContext) -> Boolean) : ActivationRule
open class CailaEntityByNameActivationRule(val entity: String) : CailaEntityActivationRule({ it.entity == entity })

fun ActivationRulesBuilder.cailaEntity(entity: String) = rule(CailaEntityByNameActivationRule(entity))