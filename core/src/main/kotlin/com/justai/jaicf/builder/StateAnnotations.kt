package com.justai.jaicf.builder

/**
 * This annotation marks functions and extension functions of a ScenarioGraphBuilder that declare a state.
 * This annotation is necessary for the functioning of the plugin and allows you to add custom functions that define a state.
 *
 * This annotation is used for a function that declares a state.
 * The annotation StateName or StateParameter should be used together
 *
 * Example:
 * ```
 * @StateDeclaration
 * fun myState(
 *  @StateParameter name: String,
 *  body: ActionContext<ActivatorContext, B, R>.() -> Unit
 * ) { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class StateDeclaration

/**
 * This annotation specifies the static name of a state. It is used together with the StateDeclaration annotation.
 * Annotate the function that creates the state
 *
 * Example
 * ```
 * @StateDeclaration
 * @StateName("myState")
 * fun myState(
 *  body: ActionContext<ActivatorContext, B, R>.() -> Unit
 * ) { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class StateName(val name: String)

/**
 * This annotation points to the parameter that takes a name of a state.
 * It is used together with the StateDeclaration annotation.
 * Annotate a parameter that takes a name of a state.
 *
 * Example:
 * ```
 * @StateDeclaration
 * fun myState(
 *  @StateParameter name: String = "myState",
 *  body: ActionContext<ActivatorContext, B, R>.() -> Unit
 * ) { ... }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class StateParameter
