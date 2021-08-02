package com.justai.jaicf.plugin

/**
 * This annotation indicates to the plugin that a StatePath will be passed to the annotated parameter or receiver.
 * This allows you to use the plugin's features, such as state navigation and inspections. To use it,
 * you need to annotate a parameter of a function or a primary constructor that takes a state name.
 * Also, you can annotate a receiver type of extensions function.
 *
 * Example:
 * ```
 * fun @PathValue String.and(@PathValue that: String)
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class PathValue()
