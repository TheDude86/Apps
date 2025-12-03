package com.mcmlr.system.products.announcements

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.AnnouncementsAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class AnnouncementsEnvironment(): Environment<AnnouncementsApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): AnnouncementsApp = AnnouncementsApp(player)

    override fun name(): String = "Announcements"

    override fun icon(): String = "http://textures.minecraft.net/texture/d6b0ce673b3f28c4610cea7ce042c850e34cc988cb0d7c803979f50dd0f15731"

    override fun permission(): String = PermissionNode.ADMIN.node

    override fun summary(): String = "Messages that server staff can write for all players to see on their home screen."
}

class AnnouncementsApp(player: Player): App(player) {
    private lateinit var appComponent: AnnouncementsAppComponent

    @Inject
    lateinit var announcementsBlock: AnnouncementsBlock

    override fun root(): Block = announcementsBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .announcementsSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
