package com.mcmlr.system.products.support

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.File
import javax.inject.Inject

class FileViewerBlock @Inject constructor(
    player: Player,
    origin: Location,
    fileEditorBlock: FileEditorBlock,
): Block(player, origin) {
    private val view = FileViewerViewController(player, origin)
    private val interactor = FileViewerInteractor(player, view, fileEditorBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setFile(file: File) {
        interactor.editingFile = file
        view.title = file.nameWithoutExtension
    }
}

class FileViewerViewController(player: Player, origin: Location): NavigationViewController(player, origin), FileViewerPresenter {
    var title = "File"

    private lateinit var fileView: ListFeedView
    private lateinit var editButton: ButtonView

    override fun setEditListener(listener: Listener) {
        editButton.addListener(listener)
    }

    override fun setFile(lines: List<String>) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                lines.forEach {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .margins(start = 50),
                        alignment = Alignment.LEFT,
                        lineWidth = 600,
                        size = 4,
                        text = it,
                    )
                }
            }
        })
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${title}",
            size = 16,
        )

        fileView = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 200, bottom = 200),
        )

        editButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(fileView)
                .alignBottomToTopOf(fileView)
                .margins(bottom = 50),
            text = "${ChatColor.GOLD}Edit",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Edit",
        )
    }
}

interface FileViewerPresenter: Presenter {
    fun setFile(lines: List<String>)
    fun setEditListener(listener: Listener)
}

class FileViewerInteractor(
    private val player: Player,
    private val presenter: FileViewerPresenter,
    private val fileEditorBlock: FileEditorBlock,
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

        val lines = mutableListOf<String>()
        file.inputStream().bufferedReader().forEachLine {
            lines.add(it)
        }

        presenter.setFile(lines)

        presenter.setEditListener(object : Listener {
            override fun invoke() {
                val file = editingFile ?: return
                fileEditorBlock.setFile(file)
                routeTo(fileEditorBlock)
            }
        })
    }

}