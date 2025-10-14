package com.mcmlr.system.products.pong

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.PongAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class PongEnvironment(): Environment<PongApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): PongApp = PongApp(player)

    override fun name(): String = "Pong"

    override fun icon(): String = "http://textures.minecraft.net/texture/232beec7cee6677fbac0132a0a05eb3ce1a6dd0803fd2804575a4f4590a83fa7"

    override fun permission(): String? = PermissionNode.PONG.node

    override fun summary(): String = "Play the classic arcade game, Pong."
}

class PongApp(player: Player): App(player) {
    private lateinit var appComponent: PongAppComponent

    @Inject
    lateinit var pongBlock: PongBlock

    override fun root(): Block = pongBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .pongSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
