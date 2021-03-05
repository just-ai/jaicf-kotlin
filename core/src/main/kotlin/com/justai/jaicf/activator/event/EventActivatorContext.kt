package com.justai.jaicf.activator.event

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.StrictActivatorContext

/**
 * Appears in the context of action block if [EventActivator] handled the user's request.
 *
 * @property event an event's name that activated a state
 * @see com.justai.jaicf.context.ActionContext
 */
open class EventActivatorContext(
    open val event: String
): StrictActivatorContext()

val ActivatorContext.event
    get() = this as? EventActivatorContext