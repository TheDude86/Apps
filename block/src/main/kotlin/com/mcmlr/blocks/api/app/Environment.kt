package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.AppManager
import com.mcmlr.blocks.api.data.InputRepository
import org.bukkit.Location
import org.bukkit.entity.Player

abstract class Environment<out T: App>: BaseEnvironment<T>() {
    lateinit var cursorRepository: InputRepository
    lateinit var parentEnvironment: BaseEnvironment<BaseApp>
    lateinit var parentApp: BaseApp

    fun launch(
        parentEnvironment: BaseEnvironment<BaseApp>,
        parentApp: BaseApp,
        appManager: AppManager,
        player: Player,
        inputRepository: InputRepository,
        deeplink: String?,
    ): App {
        this.parentEnvironment = parentEnvironment
        this.parentApp = parentApp

        val app = getInstance(player)
        app.configure(appManager, this.parentEnvironment, this.parentApp, inputRepository, deeplink)
        app.create(resources, Camera(player)) //TODO: Update Camera

        return app
    }
}