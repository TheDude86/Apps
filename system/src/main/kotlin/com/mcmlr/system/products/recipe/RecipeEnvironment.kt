package com.mcmlr.system.products.recipe

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.RecipeAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class RecipeEnvironment(): Environment<RecipeApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): RecipeApp = RecipeApp(player)

    override fun name(): String = "Recipes"

    override fun icon(): String = "http://textures.minecraft.net/texture/c2ebbdb18d747281b5462f857ee984675a39d5a0274446a22f66264a53d2b034"

    override fun permission(): String? = PermissionNode.RECIPE.node

    override fun summary(): String = "A resource for players to look up the crafting recipes for any in game item."
}

class RecipeApp(player: Player): App(player) {
    private lateinit var appComponent: RecipeAppComponent

    @Inject
    lateinit var recipesBlock: RecipesBlock

    override fun root(): Block = recipesBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .recipeSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
