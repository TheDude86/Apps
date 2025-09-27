package com.mcmlr.system.products.kits

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.KitsAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class KitsEnvironment(): Environment<KitsApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): KitsApp = KitsApp(player)

    override fun name(): String = "Kits"

    override fun icon(): String = "http://textures.minecraft.net/texture/fe7a810d2112275cc1821dcc6e29da3d2b8fc659af7290a3cb70be536ae2040a"

    override fun permission(): String? = PermissionNode.KIT.node

    override fun summary(): String = "Allows players to collect custom kits created by server staff."
}

class KitsApp(player: Player): App(player) {
    private lateinit var appComponent: KitsAppComponent

    @Inject
    lateinit var kitsBlock: KitsBlock

    override fun root(): Block = kitsBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .kitsSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
