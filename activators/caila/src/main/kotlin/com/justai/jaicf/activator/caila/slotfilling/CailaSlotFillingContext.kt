package com.justai.jaicf.activator.caila.slotfilling

import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.activator.caila.dto.CailaEntityMarkupData
import com.justai.jaicf.activator.caila.dto.CailaKnownSlotData
import com.justai.jaicf.activator.caila.dto.CailaSlotData
import com.justai.jaicf.context.ActivatorContext
import java.io.Serializable


internal data class CailaSlotFillingContext(
    val initialActivatorContext: ActivatorContext,
    val requiredSlots: List<CailaSlotData>,
    val knownSlots: MutableList<CailaKnownSlotData>,
    val knownEntities: MutableList<CailaEntityMarkupData>,
    val maxRetries: MutableMap<String, Int> = HashMap()
) : Serializable {
    companion object Factory {
        fun createInitial(initial: ActivatorContext): CailaSlotFillingContext {
            val knownSlots = initial.caila?.result?.inference?.variants?.get(0)?.slots?.toMutableList()
                ?: mutableListOf()
            val knownEntities = initial.caila?.entities?.toMutableList()
                ?: mutableListOf()
            val requiredSlots = initial.caila?.topIntent?.slots?.filter { it.required }?.toMutableList()
                ?: mutableListOf()
            val maxRetries = initial.caila?.topIntent?.slots?.map { it.name to 0 }?.toMap()?.toMutableMap()
                ?: mutableMapOf()

            return CailaSlotFillingContext(
                initialActivatorContext = initial,
                knownSlots = knownSlots,
                knownEntities = knownEntities,
                requiredSlots = requiredSlots,
                maxRetries = maxRetries
            )
        }
    }
}