package com.justai.jaicf.activator.regex

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.StrictActivatorContext
import java.util.regex.Pattern

/**
 * Appears in the context of action block if [RegexActivator] handled the user's request.
 *
 * @property pattern a regex pattern that activated a state
 * @property groups a list of groups fetched from the request's text
 * @property namedGroups a map of named groups fetched from the request's text
 */
data class RegexActivatorContext(
    val pattern: Pattern
): StrictActivatorContext() {

    val groups = mutableListOf<String>()
    val namedGroups = mutableMapOf<String, String>()
}

val ActivatorContext.regex
    get() = this as? RegexActivatorContext