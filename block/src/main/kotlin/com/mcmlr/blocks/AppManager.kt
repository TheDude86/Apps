package com.mcmlr.blocks

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.app.Environment

interface AppManager {

    fun launch(app: Environment<App>, deeplink: String? = null)

    fun launchConfig(app: ConfigurableEnvironment<ConfigurableApp>)

    fun shutdown()

    fun notifyShutdown()

    fun closeApp()
}