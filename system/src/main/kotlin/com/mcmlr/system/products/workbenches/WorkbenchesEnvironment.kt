package com.mcmlr.system.products.workbenches

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.WorkbenchesAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class WorkbenchesEnvironment(): Environment<WorkbenchesApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): WorkbenchesApp = WorkbenchesApp(player)

    override fun name(): String = "Workbenches"

    override fun icon(): String = "http://textures.minecraft.net/texture/189f1e8764beed5e33a68b6190a03486b1b4b11a3a590688c75a897b9d10d95"

    override fun permission(): String? = PermissionNode.WORKBENCH.node

    override fun summary(): String = "Opens various workbenches for the player to use."
}

class WorkbenchesApp(player: Player): App(player) {
    private lateinit var appComponent: WorkbenchesAppComponent

    @Inject
    lateinit var workbenchesBlock: WorkbenchesBlock

    override fun root(): Block = workbenchesBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .workbenchesSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
