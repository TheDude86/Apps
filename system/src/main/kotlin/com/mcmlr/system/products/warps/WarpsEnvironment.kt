package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.WarpsAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class WarpsEnvironment(): ConfigurableEnvironment<WarpsApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): WarpsApp = WarpsApp(player)

    override fun name(): String = "Warps"

    override fun icon(): String = "http://textures.minecraft.net/texture/b0bfc2577f6e26c6c6f7365c2c4076bccee653124989382ce93bca4fc9e39b"

    override fun permission(): String? = PermissionNode.WARP.node

    override fun summary(): String = "Allows server staff to set warp locations in the world that players can teleport to."
}

class WarpsApp(player: Player): ConfigurableApp(player) {
    private lateinit var appComponent: WarpsAppComponent

    @Inject
    lateinit var warpsBlock: WarpsBlock

    @Inject
    lateinit var warpsConfigBlock: WarpConfigBlock

    override fun root(): Block = warpsBlock

    override fun config(): Block = warpsConfigBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .warpsSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
