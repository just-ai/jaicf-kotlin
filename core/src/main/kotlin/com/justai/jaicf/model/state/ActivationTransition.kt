package com.justai.jaicf.model.state

data class ActivationTransition(val fromState: StatePath, val toState: StatePath) {

    constructor(fromState: String, toState: String) : this(
        StatePath.parse(fromState), StatePath.parse(toState)
    )

    private val fromPath = fromState.toString()
    private val toPath = toState.toString()

    val distance: Int
        get() {
            if (isToChild() || isIndirectChild()) return 0
            if (isToRoot()) return fromState.components.size - 1
            if (isFromRoot()) return toState.components.size - 1
            if (isSameState()) return 1

            val max = maxOf(fromState.components.size, toState.components.size)
            val common = fromPath.commonPrefixWith(toPath).count { it == '/' }
            return max - common
        }

    private fun isToChild() = toState.parent == fromPath

    private fun isSameState() = toPath == fromPath

    private fun isFromRoot() = fromPath == "/"

    private fun isToRoot() = toPath == "/"

    private fun isIndirectChild(): Boolean {
        if (isFromRoot() || isToRoot() || isSameState()) return false
        return fromPath.commonPrefixWith(toPath) == fromPath
    }
}
