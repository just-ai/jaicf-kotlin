package com.justai.jaicf.activator.catchall

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.StrictActivatorContext

/**
 * Appears in the context of action block if [CatchAllActivator] handled the user's request.
 */
open class CatchAllActivatorContext : StrictActivatorContext()

val ActivatorContext.catchAll
    get() = this as? CatchAllActivatorContext