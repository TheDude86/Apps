package com.mcmlr.blocks.api

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment

object AppInjector {
    private var callback: (Environment<App>) -> Unit = {}

    fun register(environment: Environment<App>) {
        callback.invoke(environment)
    }

    fun setInjectorListener(listener: (Environment<App>) -> Unit) {
        callback = listener
    }
}