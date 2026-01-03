package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.player.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class CreatePlaylistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    iconSelectionBlock: IconSelectionBlock,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = CreatePlaylistViewController(player, origin)
    private val interactor = CreatePlaylistInteractor(player, view, iconSelectionBlock, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setEditingPlaylist(playlist: Playlist) {
        interactor.setEditingPlaylist(playlist)
    }
}

class CreatePlaylistViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), CreatePlaylistPresenter {

    private lateinit var playlistNameButton: TextInputView
    private lateinit var playlistIconButton: ButtonView
    private lateinit var playlistIconItemButton: ItemButtonView
    private lateinit var iconContainer: ViewContainer
    private lateinit var createButton: ButtonView
    private lateinit var cancelButton: ButtonView

    override fun setEditingPlaylist(playlist: Playlist) {
        val name = playlist.name
        val iconData = playlist.icon?.data
        if (name != null) playlistNameButton.update(text = name)
        if (iconData != null) {
            playlistIconButton.visible = false
            playlistIconItemButton.visible = true
            playlistIconItemButton.item = ItemStack(Material.valueOf(iconData))
            updateTextDisplay(playlistIconButton)
            updateTextDisplay(playlistIconItemButton)
        }

        createButton.update(text = R.getString(player, S.CREATE_PLAYLIST_UPDATE_BUTTON.resource()))
    }

    override fun addCreateListener(listener: Listener) {
        createButton.addListener(listener)
    }

    override fun addCancelListener(listener: Listener) {
        cancelButton.addListener(listener)
    }

    override fun setPlaylistNameText(name: String) {
        playlistNameButton.update(text = name)
    }

    override fun addPlaylistNameTextListener(listener: TextListener) {
        playlistNameButton.addTextChangedListener(listener)
    }

    override fun addHomeIconListener(listener: Listener) {
        playlistIconButton.addListener(listener)
        playlistIconItemButton.addListener(listener)
    }

    override fun addRouteBackListener(listener: Listener) {
        backButton?.addListener(listener)
    }

    override fun setIcon(icon: Material?) {
        if (icon == null) {
            playlistIconButton.visible = true
            playlistIconItemButton.visible = false
            updateTextDisplay(playlistIconButton)
            updateTextDisplay(playlistIconItemButton)
        } else {
            playlistIconButton.visible = false
            playlistIconItemButton.visible = true
            playlistIconItemButton.update(item = ItemStack(icon))
            updateTextDisplay(playlistIconButton)
            updateTextDisplay(playlistIconItemButton)
        }
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.CREATE_PLAYLIST_TITLE.resource()),
            size = 16,
        )

        playlistNameButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}${R.getString(player, S.CREATE_PLAYLIST_NAME_PLACEHOLDER.resource())}",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}${R.getString(player, S.CREATE_PLAYLIST_NAME_PLACEHOLDER.resource())}",
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(playlistNameButton)
                .alignTopToTopOf(playlistNameButton)
                .alignBottomToBottomOf(playlistNameButton)
                .margins(end = 150),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    playlistIconButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        size = 6,
                        text = "${ChatColor.GRAY}${R.getString(player, S.CREATE_PLAYLIST_SELECT_ICON.resource())}",
                        highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}${R.getString(player, S.CREATE_PLAYLIST_SELECT_ICON.resource())}"
                    )

                    playlistIconItemButton = addItemButtonView(
                        modifier = Modifier()
                            .size(55, 55)
                            .center(),
                        item = null,
                        visible = false,
                    )
                }
            }
        )

        createButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(playlistNameButton)
                .x(200)
                .margins(top = 100),
            text = R.getString(player, S.CREATE_PLAYLIST_CREATE_BUTTON.resource())
        )

        cancelButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(playlistNameButton)
                .x(-200)
                .margins(top = 100),
            text = R.getString(player, S.CREATE_PLAYLIST_CANCEL_BUTTON.resource())
        )
    }

}

interface CreatePlaylistPresenter: Presenter {
    fun addPlaylistNameTextListener(listener: TextListener)

    fun setPlaylistNameText(name: String)

    fun addHomeIconListener(listener: Listener)

    fun setIcon(icon: Material?)

    fun addCreateListener(listener: Listener)

    fun addCancelListener(listener: Listener)

    fun setEditingPlaylist(playlist: Playlist)

    fun addRouteBackListener(listener: Listener)
}

class CreatePlaylistInteractor(
    private val player: Player,
    private val presenter: CreatePlaylistPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {

    private var playlistName: String? = null
    private var playlistIcon: Material? = null
    private var editingPlaylist: Playlist? = null

    fun setEditingPlaylist(playlist: Playlist) {
        editingPlaylist = playlist
    }

    override fun onCreate() {
        super.onCreate()

        if (playlistName == null && playlistIcon == null) {
            editingPlaylist?.let {
                presenter.setEditingPlaylist(it)
                playlistName = it.name

                val iconData = it.icon?.data ?: return@let
                playlistIcon = Material.valueOf(iconData)
            }
        }

        playlistName?.let {
            presenter.setPlaylistNameText(it)
        }

        presenter.addPlaylistNameTextListener(listener = object : TextListener {
            override fun invoke(text: String) {
                playlistName = text
            }
        })

        presenter.addHomeIconListener(object : Listener {
            override fun invoke() {
                routeTo(iconSelectionBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                        presenter.setIcon(icon?.type)
                        playlistIcon = icon?.type
                    }
                })
            }
        })

        presenter.addCreateListener(object : Listener {
            override fun invoke() {
                val name = playlistName
                val icon = playlistIcon

                val editingPlaylist = editingPlaylist
                if (editingPlaylist != null) {
                    val uuid = editingPlaylist.uuid ?: return
                    libraryRepository.updatePlaylist(icon?.name, name, uuid)?.invokeOnCompletion {
                        CoroutineScope(DudeDispatcher()).launch {
                            this@CreatePlaylistInteractor.editingPlaylist = null
                            routeBack()
                        }
                    }
                } else {
                    libraryRepository.createPlaylist(icon?.name, name)?.invokeOnCompletion {
                        CoroutineScope(DudeDispatcher()).launch {
                            routeBack()
                        }
                    }
                }
            }
        })

        presenter.addCancelListener(object : Listener {
            override fun invoke() {
                routeBack()
            }
        })

        presenter.addRouteBackListener(object : Listener {
            override fun invoke() {
                editingPlaylist = null
            }
        })
    }
}
