package com.mcmlr.system.products.info

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import org.bukkit.entity.Player

class InfoEnvironment(): Environment<InfoApp>() {
    override fun build() {
        TODO("Not yet implemented")
    }

    override fun getInstance(player: Player): InfoApp {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        TODO("Not yet implemented")
    }

    override fun icon(): String {
        TODO("Not yet implemented")
    }

    override fun permission(): String? {
        TODO("Not yet implemented")
    }

    override fun summary(): String {
        TODO("Not yet implemented")
    }
}

class InfoApp(player: Player): App(player){
    override fun root(): Block {
        TODO("Not yet implemented")
    }

    override fun onCreate(child: Boolean) {
        TODO("Not yet implemented")
    }
}
