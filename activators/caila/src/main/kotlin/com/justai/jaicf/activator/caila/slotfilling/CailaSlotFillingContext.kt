package com.justai.jaicf.activator.caila.slotfilling

import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.activator.caila.dto.CailaEntityMarkupData
import com.justai.jaicf.activator.caila.dto.CailaSlotData
import com.justai.jaicf.activator.caila.dto.CailaKnownSlotData
import com.justai.jaicf.context.ActivatorContext

internal data class CailaSlotFillingContext(
    val initialActivatorContext: ActivatorContext,
    val requiredSlots: List<CailaSlotData>,
    val knownSlots: MutableList<CailaKnownSlotData>,
    val knownEntities: MutableList<CailaEntityMarkupData>,
    val maxRetries: MutableMap<String, Int> = HashMap()
) {
    companion object Factory {
        fun createInitial(initial: ActivatorContext) = CailaSlotFillingContext(
            initialActivatorContext = initial,
            knownSlots = initial.caila?.result?.inference?.variants?.get(0)?.slots?.toMutableList() ?: mutableListOf(),
            knownEntities = initial.caila?.entities?.toMutableList() ?: mutableListOf(),
            requiredSlots = initial.caila?.topIntent?.slots?.toMutableList() ?: mutableListOf(),
            maxRetries = initial.caila?.topIntent?.slots?.map { it.name to 0 }?.toMap()?.toMutableMap() ?: mutableMapOf()
        )
    }
}