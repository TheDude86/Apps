package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.TeleportAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class TeleportEnvironment(): Environment<TeleportApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): TeleportApp = TeleportApp(player)

    override fun name(): String = "Teleport"

    override fun icon(): String = "http://textures.minecraft.net/texture/b1dd4fe4a429abd665dfdb3e21321d6efa6a6b5e7b956db9c5d59c9efab25"

    override fun permission(): String? = PermissionNode.TELEPORT.node

    override fun summary(): String = "Allows players to teleport to other online players on the server."
}

class TeleportApp(player: Player): App(player) {
    private lateinit var appComponent: TeleportAppComponent

    @Inject
    lateinit var teleportBlock: TeleportBlock

    override fun root(): Block = teleportBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .teleportSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
