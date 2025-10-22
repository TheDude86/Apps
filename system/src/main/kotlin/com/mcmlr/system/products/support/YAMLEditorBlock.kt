package com.mcmlr.system.products.support

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import javax.inject.Inject

class YAMLEditorBlock @Inject constructor(
    player: Player,
    origin: Location,
): Block(player, origin) {
    private val view = YAMLEditorViewController(player, origin)
    private val interactor = YAMLEditorInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setFile(file: File) {
        interactor.editingFile = file
    }
}

class YAMLEditorViewController(player: Player, origin: Location): NavigationViewController(player, origin), YAMLEditorPresenter {

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

interface YAMLEditorPresenter: Presenter {

}

class YAMLEditorInteractor(
    private val player: Player,
    private val presenter: YAMLEditorPresenter,
): Interactor(presenter) {

    var editingFile: File? = null

    override fun onCreate() {
        super.onCreate()
        val file = editingFile

        if (file == null) {
            player.sendMessage("${ChatColor.RED}Error: No file has been selected!")
            return
        } else if (!file.exists()) {
            player.sendMessage("${ChatColor.RED}Error: file ${ChatColor.BOLD}${file.name} ${ChatColor.RED}doesn't exist!")
            return
        }

        val config = YamlConfiguration.loadConfiguration(file)

    }

}