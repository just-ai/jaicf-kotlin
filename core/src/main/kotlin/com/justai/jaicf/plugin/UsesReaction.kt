package com.justai.jaicf.plugin

/**
 * This annotation indicates that the Reactions and derived classes extension function uses a reaction that must
 * be redefined to work correctly.
 * The plugin inspects that the method is overridden
 *
 * Example:
 * ```
 * @UsesReaction("image")
 * fun Reactions.myImage(url: String): ImageReaction { ... }
 * ```
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UsesReaction(val name: String)
