package com.mcmlr.system.products.announcements

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock.Companion.ANNOUNCEMENT_BUNDLE_KEY
import com.mcmlr.system.products.announcements.AnnouncementSelectorBlock.Companion.ANNOUNCEMENT_SELECT_BUNDLE_KEY
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AnnouncementsBlock @Inject constructor(
    player: Player,
    origin: Origin,
    announcementEditorBlock: AnnouncementEditorBlock,
    announcementSelectorBlock: AnnouncementSelectorBlock,
): Block(player, origin) {
    private val view = AnnouncementsViewController(player, origin)
    private val interactor = AnnouncementsInteractor(player, view, announcementEditorBlock, announcementSelectorBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AnnouncementsViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), AnnouncementsPresenter {

    private lateinit var createButton: ButtonView
    private lateinit var updateButton: ButtonView
    private lateinit var statusMessage: TextView

    override fun setStatusMessageText(text: String) {
        statusMessage.update(text = text)
    }

    override fun setCreateListener(listener: Listener) = createButton.addListener(listener)

    override fun setUpdateListener(listener: Listener) = updateButton.addListener(listener)

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${R.getString(player, S.ANNOUNCEMENTS.resource())}",
            size = 16,
        )

        val message = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .center(),
            text = R.getString(player, S.ANNOUNCEMENTS_MESSAGE.resource()),
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
            text = "${ChatColor.GOLD}${R.getString(player, S.CREATE_NEW_POST.resource())}",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CREATE_NEW_POST.resource())}"
        )

        updateButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(350)
                .alignTopToBottomOf(message)
                .margins(top = 50),
            text = "${ChatColor.GOLD}${R.getString(player, S.EDIT_POSTS.resource())}",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.EDIT_POSTS.resource())}"
        )
    }

}

interface AnnouncementsPresenter: Presenter {
    fun setStatusMessageText(text: String)

    fun setCreateListener(listener: Listener)
    fun setUpdateListener(listener: Listener)
}

class AnnouncementsInteractor(
    private val player: Player,
    private val presenter: AnnouncementsPresenter,
    private val announcementEditorBlock: AnnouncementEditorBlock,
    private val announcementSelectorBlock: AnnouncementSelectorBlock,
): Interactor(presenter) {



    //TODO: How sketchy is this... too stoned to tell (:D)
    private val editorCallback: RouteToCallback = object : RouteToCallback {
        override fun invoke(bundle: Bundle) {
            val model = bundle.getData<AnnouncementEditorResult>(ANNOUNCEMENT_BUNDLE_KEY)

            val statusMessage = when (model) {
                AnnouncementEditorResult.CREATE -> "${ChatColor.GREEN}${R.getString(player, S.ANNOUNCEMENT_CREATED_MESSAGE.resource())}"
                AnnouncementEditorResult.UPDATE -> "${ChatColor.GREEN}${R.getString(player, S.ANNOUNCEMENT_UPDATED_MESSAGE.resource())}"
                AnnouncementEditorResult.DELETE -> "${ChatColor.GREEN}${R.getString(player, S.ANNOUNCEMENT_DELETED_MESSAGE.resource())}"
                null -> ""
            }

            presenter.setStatusMessageText(statusMessage)
        }
    }

    private val selectorCallback: RouteToCallback = object : RouteToCallback {
        override fun invoke(bundle: Bundle) {
            val model = bundle.getData<AnnouncementModel>(ANNOUNCEMENT_SELECT_BUNDLE_KEY)
            if (model != null) {
                announcementEditorBlock.setSelectedAnnouncement(model)
                routeTo(announcementEditorBlock, editorCallback)
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        presenter.setCreateListener(object : Listener {
            override fun invoke() {
                announcementEditorBlock.setSelectedAnnouncement(null)
                routeTo(announcementEditorBlock, editorCallback)
            }
        })

        presenter.setUpdateListener(object : Listener {
            override fun invoke() {
                routeTo(announcementSelectorBlock, selectorCallback)
            }
        })
    }
}
