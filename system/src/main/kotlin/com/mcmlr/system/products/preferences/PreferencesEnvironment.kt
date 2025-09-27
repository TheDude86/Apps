package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.PreferencesAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class PreferencesEnvironment(): Environment<PreferencesApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): PreferencesApp = PreferencesApp(player)

    override fun name(): String = "Preferences"

    override fun icon(): String = "http://textures.minecraft.net/texture/1e5edfe90156ce9b5b6b80793dc2cbfe850ddec856c99eebde31775cce956041"

    override fun permission(): String? = PermissionNode.PREFERENCES.node

    override fun summary(): String = "User settings that customize Apps for specific users."
}

class PreferencesApp(player: Player): App(player) {
    private lateinit var appComponent: PreferencesAppComponent

    @Inject
    lateinit var preferencesBlock: PreferencesBlock

    override fun root(): Block = preferencesBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .preferencesSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
