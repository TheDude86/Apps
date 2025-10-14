package com.mcmlr.system.products.announcements

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock.Companion.ANNOUNCEMENT_BUNDLE_KEY
import com.mcmlr.system.products.announcements.AnnouncementSelectorBlock.Companion.ANNOUNCEMENT_SELECT_BUNDLE_KEY
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AnnouncementsBlock @Inject constructor(
    player: Player,
    camera: Camera,
    announcementEditorBlock: AnnouncementEditorBlock,
    announcementSelectorBlock: AnnouncementSelectorBlock,
): Block(player, camera) {
    private val view = AnnouncementsViewController(player, camera)
    private val interactor = AnnouncementsInteractor(view, announcementEditorBlock, announcementSelectorBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AnnouncementsViewController(
    private val player: Player,
    camera: Camera,
): NavigationViewController(player, camera), AnnouncementsPresenter {

    private lateinit var createButton: ButtonView
    private lateinit var updateButton: ButtonView
    private lateinit var statusMessage: TextView

    override fun setStatusMessageText(text: String) {
        statusMessage.setTextView(text)
    }

    override fun setCreateListener(listener: () -> Unit) = createButton.addListener(listener)

    override fun setUpdateListener(listener: () -> Unit) = updateButton.addListener(listener)

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Announcements",
            size = 16,
        )

        val message = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .center(),
            text = "Announcements appear on the home screen of ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET}.  You can create new posts or edit/delete existing posts here.",
            size = 6,
        )

        statusMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignBottomToTopOf(message)
                .centerHorizontally(),
            text = "",
        )

        createButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(-350)
                .alignTopToBottomOf(message)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Create New Post",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create New Post"
        )

        updateButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(350)
                .alignTopToBottomOf(message)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Edit Posts",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Edit Posts"
        )
    }

}

interface AnnouncementsPresenter: Presenter {
    fun setStatusMessageText(text: String)

    fun setCreateListener(listener: () -> Unit)
    fun setUpdateListener(listener: () -> Unit)
}

class AnnouncementsInteractor(
    private val presenter: AnnouncementsPresenter,
    private val announcementEditorBlock: AnnouncementEditorBlock,
    private val announcementSelectorBlock: AnnouncementSelectorBlock,
): Interactor(presenter) {



    //TODO: How sketchy is this... too stoned to tell (:D)
    private val editorCallback: (Bundle) -> Unit = { bundle ->
        val model = bundle.getData<AnnouncementEditorResult>(ANNOUNCEMENT_BUNDLE_KEY)

        val statusMessage = when (model) {
            AnnouncementEditorResult.CREATE -> "${ChatColor.GREEN}Your announcement has been created, you can see it on your home screen now!"
            AnnouncementEditorResult.UPDATE -> "${ChatColor.GREEN}Your announcement has been updated, you can see your changes on your home screen now!"
            AnnouncementEditorResult.DELETE -> "${ChatColor.GREEN}Your announcement has been deleted, it has been removed from everyone's feed."
            null -> ""
        }

        presenter.setStatusMessageText(statusMessage)
    }

    private val selectorCallback: (Bundle) -> Unit = { bundle ->
        val model = bundle.getData<AnnouncementModel>(ANNOUNCEMENT_SELECT_BUNDLE_KEY)
        if (model != null) {
            announcementEditorBlock.setSelectedAnnouncement(model)
            routeTo(announcementEditorBlock, editorCallback)
        }
    }


    override fun onCreate() {
        super.onCreate()

        presenter.setCreateListener {
            announcementEditorBlock.setSelectedAnnouncement(null)
            routeTo(announcementEditorBlock, editorCallback)
        }

        presenter.setUpdateListener {
            routeTo(announcementSelectorBlock, selectorCallback)
        }
    }
}
