package com.mcmlr.system.products.spawn

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.SpawnAppComponent
import com.mcmlr.system.products.data.PermissionNode
import com.mcmlr.system.products.settings.SpawnConfigBlock
import org.bukkit.entity.Player
import javax.inject.Inject

class SpawnEnvironment(): ConfigurableEnvironment<SpawnApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): SpawnApp = SpawnApp(player)

    override fun name(): String = "Spawn"

    override fun icon(): String = "http://textures.minecraft.net/texture/9ad094dca5987a0ba2c3f98a2c8981a86f68b0f03b0ddda550d09f1defbcf207"

    override fun permission(): String? = PermissionNode.SPAWN.node

    override fun summary(): String = "Changes spawning behavior allowing server staff to change the server spawn and where players are teleported to when they respawn."
}

class SpawnApp(player: Player): ConfigurableApp(player) {
    private lateinit var appComponent: SpawnAppComponent

    @Inject
    lateinit var spawnBlock: SpawnBlock

    @Inject
    lateinit var spawnConfigBlock: SpawnConfigBlock

    override fun root(): Block = spawnBlock

    override fun config(): Block = spawnConfigBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .spawnSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
