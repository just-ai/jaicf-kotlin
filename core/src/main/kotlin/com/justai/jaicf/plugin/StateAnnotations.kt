package com.justai.jaicf.plugin

/**
 * This annotation marks functions and extension functions of a ScenarioGraphBuilder that declare a state.
 * This annotation is necessary for the functioning of the plugin and allows you to add custom functions that define a state.
 * The annotation StateName can be used together. If there is no a @StateName annotation, then the name parameter should be specified.
 * The annotation StateBody should be used together.
 *
 * Examples:
 * ```
 * @StateDeclaration
 * fun myState(
 *  @StateName name: String,
 *  @StateBody body: ActionContext<ActivatorContext, B, R>.() -> Unit
 * ) { ... }
 *
 * @StateDeclaration("My other state")
 * fun myOtherState(
 *  @StateBody body: ActionContext<ActivatorContext, B, R>.() -> Unit
 * ) { ... }
 * ```
 *
 * @param name - name of state if there is not a @StateName annotation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class StateDeclaration(val name: String = "")

/**
 * This annotation points to the parameter that takes a name of a state. It is used together with the StateDeclaration annotation.
 * To use it, you need to annotate a parameter of a function or a primary constructor that takes a name of a state.
 * Also, you can annotate a receiver type of extensions function.
 *
 * Example:
 * ```
 * @StateDeclaration
 * fun myState(
 *  @StateName name: String = "myState",
 *  @StateBody body: StateBuilder<B, R>.() -> Unit
 * ) { ... }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class StateName

/**
 * This annotation points to a body of the state, or other words to the parameter that takes a StateBuilder lambda.
 * It is used together with the StateDeclaration annotation.
 * Annotate a parameter that takes a StateBuilder lambda or lambda expression inside a function annotated with StateDeclaration.
 *
 * Example:
 * ```
 * @StateDeclaration
 * fun myState(
 *  @StateName name: String = "myState",
 *  @StateBody body: StateBuilder<B, R>.() -> Unit
 * ) { ... }
 *
 * @StateDeclaration
 * fun myOtherState(@StateName name: String) {
 *     state(name) @StateBody {
 *         ...
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class StateBody
