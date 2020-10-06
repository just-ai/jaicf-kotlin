package com.justai.jaicf.activator.regex

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.StrictActivatorContext
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Appears in the context of action block if [RegexActivator] handled the user's request.
 *
 * @property pattern a regex pattern that activated a state
 * @property matcher a matching result, contains matched groups that can be retrieved by index or name
 *
 * @see Pattern
 * @see Matcher
 */
data class RegexActivatorContext(
    val pattern: Pattern,
    val matcher: Matcher
): StrictActivatorContext()

val ActivatorContext.regex
    get() = this as? RegexActivatorContext