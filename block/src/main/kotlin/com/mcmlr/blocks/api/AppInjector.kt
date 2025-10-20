package com.mcmlr.blocks.api

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment

object AppInjector {
    val EMPTY_APP_INJECTION_LISTENER = EmptyAppInjectionListener()

    private var callback: AppInjectionListener = EMPTY_APP_INJECTION_LISTENER

    fun register(environment: Environment<App>) {
        callback.invoke(environment)
    }

    fun setInjectorListener(listener: AppInjectionListener) {
        callback = listener
    }
}

interface AppInjectionListener {
    fun invoke(environment: Environment<App>)
}

class EmptyAppInjectionListener: AppInjectionListener {
    override fun invoke(environment: Environment<App>) {}
}
