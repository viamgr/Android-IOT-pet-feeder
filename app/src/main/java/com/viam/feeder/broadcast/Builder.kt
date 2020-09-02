package com.viam.feeder.broadcast;

import android.content.IntentFilter;

/**
 * @author Aidan Follestad (@afollestad)
 */
class Builder internal constructor() {

    private val filter = IntentFilter()
    private val instructions = mutableListOf<Instructions>()

    fun onAction(
        action: String,
        execution: Execution
    ) {
        filter.addAction(action)
        instructions.add(Instructions.OnAction(action, execution))
    }

    fun onDataScheme(
        scheme: String,
        execution: Execution
    ) {
        filter.addDataScheme(scheme)
        instructions.add(Instructions.OnDataScheme(scheme, execution))
    }

    fun onCategory(
        category: String,
        execution: Execution
    ) {
        filter.addCategory(category)
        instructions.add(Instructions.OnCategory(category, execution))
    }

    internal fun filter() = filter

    internal fun instructions() = instructions
}