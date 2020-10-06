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
): StrictActivatorContext() {

    /**
     * Returns the input subsequence captured by the given group index.
     *
     * @param index The index of a capturing group in this matcher's pattern
     *
     * @return The (possibly empty) subsequence captured by the group,
     *         or null if the group failed to match part of the input
     */
    fun group(index: Int): String? = matcher.group(index)

    /**
     * Returns the input subsequence captured by the given named-capturing group.
     *
     * @param name The name of a named-capturing group in this matcher's pattern
     *
     * @return The (possibly empty) subsequence captured by the named group,
     *         or null if the group failed to match part of the input
     */
    fun group(name: String): String? = matcher.group(name)
}

val ActivatorContext.regex
    get() = this as? RegexActivatorContext