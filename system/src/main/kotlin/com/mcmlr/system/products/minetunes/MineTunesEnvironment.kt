package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.MineTunesAppComponent
import com.mcmlr.system.products.data.PermissionNode
import com.mcmlr.system.products.minetunes.blocks.MineTunesBlock
import org.bukkit.entity.Player
import javax.inject.Inject

class MineTunesEnvironment: Environment<MineTunesApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): MineTunesApp = MineTunesApp(player)

    override fun name(): String = "Mine Tunes"

    override fun icon(): String = "http://textures.minecraft.net/texture/ac62fd0d4123deeeb824a00a15b09b96039ed33599c077ef67821226a18f1f11"

    override fun permission(): String? = PermissionNode.MUSIC.node

    override fun summary(): String = "A music player that streams .nbs files."
}

class MineTunesApp(player: Player): App(player) {
    private lateinit var appComponent: MineTunesAppComponent

    @Inject
    lateinit var mineTunesBlock: MineTunesBlock

    override fun root(): Block = mineTunesBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .mineTunesSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }

}
