package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.AppManager
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.collectOn
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
        origin: Origin,
        deeplink: String?,
        useSystem: Boolean,
    ): App {
        this.parentEnvironment = parentEnvironment
        this.parentApp = parentApp

        R.loadStrings(name(), player.locale)
        val app = getInstance(player)
        app.configure(appManager, this.parentEnvironment, this.parentApp, inputRepository, deeplink, origin)
        app.create(resources, useSystem)

        return app
    }
}