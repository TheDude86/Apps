package com.mcmlr.apps

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

class TestEnvironment: Environment<TestApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): TestApp = TestApp(player)

    override fun name(): String = "Test"

    override fun icon(): String = "http://textures.minecraft.net/texture/de99a406150048fa2673160409ca686662ce0f1c7a4ee54c20ecc901c9d0bd6f"

    override fun summary(): String = "A demo app for testing purposes.  This is meant to simulate a 3rd party application using the Apps API."

    override fun permission(): String? = null
}

class TestApp(player: Player): App(player) {

    override fun root(): Block {
        return TestBlock(player, camera)
    }

    override fun onCreate(child: Boolean) {}

}

class TestBlock(player: Player, camera: Camera,): Block(player, camera) {
    private val view = TestViewController(player, camera)
    private val interactor = TestInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TestViewController(player: Player, camera: Camera,): NavigationViewController(player, camera), TestPresenter {

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 250),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Third party!",
            size = 16,
        )
    }
}

interface TestPresenter: Presenter {

}

class TestInteractor(
    player: Player,
    presenter: TestPresenter,
): Interactor(presenter) {

}
