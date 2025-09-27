package com.mcmlr.blocks

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment

interface AppManager {

    fun launch(app: Environment<App>, deeplink: String? = null)

    fun shutdown()

    fun notifyShutdown()

    fun closeApp()
}