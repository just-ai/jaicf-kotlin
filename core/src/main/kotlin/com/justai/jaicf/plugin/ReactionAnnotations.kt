package com.justai.jaicf.plugin

/**
 * This annotation indicates to the plugin that a StatePath will be passed to the annotated parameter or receiver.
 * This allows you to use the plugin's features, such as state navigation and inspections.
 * To use it, you need to annotate parameters at functions or receivers or fields at primary constructors
 *
 * Example:
 * ```
 * fun @PathValue String.and(@PathValue that: String)
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class PathValue()

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
