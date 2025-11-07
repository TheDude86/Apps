package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.AppManager
import com.mcmlr.blocks.api.data.InputRepository
import org.bukkit.Location
import org.bukkit.entity.Player

abstract class ConfigurableEnvironment<out T:  ConfigurableApp>: Environment<T>() {

    fun launchConfig(
        parentEnvironment: BaseEnvironment<BaseApp>,
        parentApp: BaseApp,
        appManager: AppManager,
        player: Player,
        inputRepository: InputRepository,
        origin: Location,
        deeplink: String?,
    ): ConfigurableApp {
        this.parentEnvironment = parentEnvironment
        this.parentApp = parentApp

        R.loadStrings(name(), player.locale)
        val app = getInstance(player)
        app.configure(appManager, this.parentEnvironment, this.parentApp, inputRepository, deeplink, origin)
        app.createConfig(resources)

        return app
    }

}