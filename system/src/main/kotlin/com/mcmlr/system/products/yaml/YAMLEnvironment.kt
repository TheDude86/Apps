package com.mcmlr.system.products.yaml

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.YAMLAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class YAMLEnvironment(): Environment<YAMLApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): YAMLApp = YAMLApp(player)

    override fun name(): String = "Files"

    override fun icon(): String = "http://textures.minecraft.net/texture/c73e8bd3c43c4514c76481ca1daf55149dfc93bd1bcfa8ab9437b9f7eb3392d9"

    override fun permission(): String = PermissionNode.ADMIN.node

    override fun summary(): String = "A file editor to edit plugin configs in game."
}

class YAMLApp(player: Player): App(player) {
    private lateinit var appComponent: YAMLAppComponent

    @Inject
    lateinit var yamlBlock: YAMLBlock

    override fun root(): Block = yamlBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .yamlSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}