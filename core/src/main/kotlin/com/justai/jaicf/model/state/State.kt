package com.justai.jaicf.model.state

import com.justai.jaicf.model.ActionAdapter

data class State(
    val path: StatePath,
    val noContext: Boolean = false,
    val modal: Boolean = false,
    val action: ActionAdapter? = null
)