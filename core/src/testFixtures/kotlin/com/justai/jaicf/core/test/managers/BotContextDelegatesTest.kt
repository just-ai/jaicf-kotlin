package com.justai.jaicf.core.test.managers

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.helpers.context.clientProperty
import com.justai.jaicf.helpers.context.with
import com.justai.jaicf.helpers.context.withContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse

open class BotContextDelegatesTest(override val manager: BotContextManager) : BotContextManagerTest {
    // @formatter:off
    var BotContextHolder.generatedNameSaveFalseRemoveFalse by clientProperty<String?> { "value" } withContext { context }
    var BotContextHolder.generatedNameSaveFalseRemoveTrue by clientProperty<String?>(saveDefault = false, removeOnNull = true) { "value" } withContext { context }
    var BotContextHolder.generatedNameSaveTrueRemoveFalse by clientProperty<String?>(saveDefault = true, removeOnNull = false) { "value" } withContext { context }
    var BotContextHolder.generatedNameSaveTrueRemoveTrue by clientProperty<String?>(saveDefault = true, removeOnNull = true) { "value" } withContext { context }
    var BotContextHolder.customNameWithoutDefaultSaveFalseRemoveFalse by clientProperty<String?>(key = "name") withContext { context }
    var BotContextHolder.customNameWithoutDefaultSaveFalseRemoveTrue by clientProperty<String?>(key = "name", saveDefault = false, removeOnNull = true) withContext { context }
    var BotContextHolder.customNameWithoutDefaultSaveTrueRemoveFalse by clientProperty<String?>(key = "name", saveDefault = true, removeOnNull = false) withContext { context }
    var BotContextHolder.customNameWithoutDefaultSaveTrueRemoveTrue by clientProperty<String?>(key = "name", saveDefault = true, removeOnNull = true) withContext { context }
    var BotContextHolder.customNameWithCustomDefaultSaveFalseRemoveFalse by clientProperty<String?>(key = "name").with({ context }) { customData }
    var BotContextHolder.customNameWithCustomDefaultSaveFalseRemoveTrue by clientProperty<String?>(key = "name", saveDefault = false, removeOnNull = true).with({ context }) { customData }
    var BotContextHolder.customNameWithCustomDefaultSaveTrueRemoveFalse by clientProperty<String?>(key = "name", saveDefault = true, removeOnNull = false).with({ context }) { customData }
    var BotContextHolder.customNameWithCustomDefaultSaveTrueRemoveTrue by clientProperty<String?>(key = "name", saveDefault = true, removeOnNull = true).with({ context }) { customData }
    // @formatter:on

    @Test
    fun generatedNameSaveFalseRemoveFalse() = testDelegateProperty(
        { generatedNameSaveFalseRemoveFalse },
        { generatedNameSaveFalseRemoveFalse = it },
        expectedName = "generatedNameSaveFalseRemoveFalse",
        hasDefault = true,
        saveDefault = false,
        removeOnNull = false
    )

    @Test
    fun generatedNameSaveFalseRemoveTrue() = testDelegateProperty(
        { generatedNameSaveFalseRemoveTrue },
        { generatedNameSaveFalseRemoveTrue = it },
        expectedName = "generatedNameSaveFalseRemoveTrue",
        hasDefault = true,
        saveDefault = false,
        removeOnNull = true
    )

    @Test
    fun generatedNameSaveTrueRemoveFalse() = testDelegateProperty(
        { generatedNameSaveTrueRemoveFalse },
        { generatedNameSaveTrueRemoveFalse = it },
        expectedName = "generatedNameSaveTrueRemoveFalse",
        hasDefault = true,
        saveDefault = true,
        removeOnNull = false
    )

    @Test
    fun generatedNameSaveTrueRemoveTrue() = testDelegateProperty(
        { generatedNameSaveTrueRemoveTrue },
        { generatedNameSaveTrueRemoveTrue = it },
        expectedName = "generatedNameSaveTrueRemoveTrue",
        hasDefault = true,
        saveDefault = true,
        removeOnNull = true
    )

    @Test
    fun customNameWithoutDefaultSaveFalseRemoveFalse() = testDelegateProperty(
        { customNameWithoutDefaultSaveFalseRemoveFalse },
        { customNameWithoutDefaultSaveFalseRemoveFalse = it },
        expectedName = "name",
        hasDefault = false,
        saveDefault = false,
        removeOnNull = false
    )

    @Test
    fun customNameWithoutDefaultSaveFalseRemoveTrue() = testDelegateProperty(
        { customNameWithoutDefaultSaveFalseRemoveTrue },
        { customNameWithoutDefaultSaveFalseRemoveTrue = it },
        expectedName = "name",
        hasDefault = false,
        saveDefault = false,
        removeOnNull = true
    )

    @Test
    fun customNameWithoutDefaultSaveTrueRemoveFalse() = testDelegateProperty(
        { customNameWithoutDefaultSaveTrueRemoveFalse },
        { customNameWithoutDefaultSaveTrueRemoveFalse = it },
        expectedName = "name",
        hasDefault = false,
        saveDefault = true,
        removeOnNull = false
    )

    @Test
    fun customNameWithoutDefaultSaveTrueRemoveTrue() = testDelegateProperty(
        { customNameWithoutDefaultSaveTrueRemoveTrue },
        { customNameWithoutDefaultSaveTrueRemoveTrue = it },
        expectedName = "name",
        hasDefault = false,
        saveDefault = true,
        removeOnNull = true
    )

    @Test
    fun customNameWithCustomDefaultSaveFalseRemoveFalse() = testDelegateProperty(
        { customNameWithCustomDefaultSaveFalseRemoveFalse },
        { customNameWithCustomDefaultSaveFalseRemoveFalse = it },
        expectedName = "name",
        hasDefault = true,
        isCustomDefault = true,
        saveDefault = false,
        removeOnNull = false
    )

    @Test
    fun customNameWithCustomDefaultSaveFalseRemoveTrue() = testDelegateProperty(
        { customNameWithCustomDefaultSaveFalseRemoveTrue },
        { customNameWithCustomDefaultSaveFalseRemoveTrue = it },
        expectedName = "name",
        hasDefault = true,
        isCustomDefault = true,
        saveDefault = false,
        removeOnNull = true
    )

    @Test
    fun customNameWithCustomDefaultSaveTrueRemoveFalse() = testDelegateProperty(
        { customNameWithCustomDefaultSaveTrueRemoveFalse },
        { customNameWithCustomDefaultSaveTrueRemoveFalse = it },
        expectedName = "name",
        hasDefault = true,
        isCustomDefault = true,
        saveDefault = true,
        removeOnNull = false
    )

    @Test
    fun customNameWithCustomDefaultSaveTrueRemoveTrue() = testDelegateProperty(
        { customNameWithCustomDefaultSaveTrueRemoveTrue },
        { customNameWithCustomDefaultSaveTrueRemoveTrue = it },
        expectedName = "name",
        hasDefault = true,
        isCustomDefault = true,
        saveDefault = true,
        removeOnNull = true
    )

    fun testDelegateProperty(
        getProp: BotContextHolder.() -> String?,
        setProp: BotContextHolder.(String?) -> Unit,
        expectedName: String,
        hasDefault: Boolean,
        isCustomDefault: Boolean = false,
        saveDefault: Boolean,
        removeOnNull: Boolean
    ) {
        val context = manager.loadContext()
        val holder = BotContextHolder(context, "customData")
        assertFalse(context.client.containsKey(expectedName))

        if (hasDefault) {
            val actual = holder.getProp()
            val expected = if (isCustomDefault) holder.customData else "value"
            assertEquals(saveDefault, context.client.containsKey(expectedName))
            assertEquals(expected, actual)
            if (saveDefault) {
                assertEquals(expected, context.client[expectedName])
            }
        } else {
            assertThrows<NoSuchElementException> { holder.getProp() }
            assertFalse(context.client.containsKey(expectedName))
        }

        holder.setProp("new value")
        val value = holder.getProp()
        assertEquals("new value", context.client[expectedName])
        assertEquals("new value", value)

        holder.setProp(null)
        assertEquals(!removeOnNull, context.client.containsKey(expectedName))
    }

    class BotContextHolder(val context: BotContext, val customData: String)
}