package com.mcmlr.system.products.info

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.TutorialAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class TutorialEnvironment(): Environment<TutorialApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): TutorialApp = TutorialApp(player)

    override fun name(): String = "Tutorial"

    override fun icon(): String = "http://textures.minecraft.net/texture/fa2afa7bb063ac1ff3bbe08d2c558a7df2e2bacdf15dac2a64662dc40f8fdbad"

    override fun permission(): String? = PermissionNode.TUTORIAL.node

    override fun summary(): String = "A brief demonstration for how Apps works, a helpful resource for players who haven't used Apps before."
}

class TutorialApp(player: Player): App(player) {
    private lateinit var appComponent: TutorialAppComponent

    @Inject
    lateinit var tutorialBlock: TutorialBlock

    override fun root(): Block = tutorialBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .tutorialSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
