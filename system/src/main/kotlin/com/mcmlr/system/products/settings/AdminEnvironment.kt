package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.dagger.AdminAppComponent
import com.mcmlr.system.SystemApp
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class AdminEnvironment(): Environment<AdminApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): AdminApp = AdminApp(player)

    override fun name(): String = "Admin"

    override fun icon(): String = "http://textures.minecraft.net/texture/d0d5a4eba140cbb34d45e40d62dd3f7e6584b2ab0b4155a0bcc603d5d750f793"

    override fun permission(): String? = PermissionNode.ADMIN.node

    override fun summary(): String = "Settings only for server staff that effects Apps for all players."
}

class AdminApp(player: Player): App(player) {
    private lateinit var appComponent: AdminAppComponent

    @Inject
    lateinit var adminBlock: AdminBlock

    override fun root(): Block = adminBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .adminSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }

}
