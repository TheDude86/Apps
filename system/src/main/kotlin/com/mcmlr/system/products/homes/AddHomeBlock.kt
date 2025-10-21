package com.mcmlr.system.products.homes

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class AddHomeBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val iconSelectionBlock: IconSelectionBlock,
    private val homesRepository: HomesRepository,
): Block(player, origin) {
    private val view = AddHomeViewController(player, origin)
    private val interactor = AddHomeInteractor(view, iconSelectionBlock, player, homesRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AddHomeViewController(
    player: Player,
    origin: Location
): NavigationViewController(player, origin), AddHomePresenter {

    private lateinit var homeNameButton: TextInputView
    private lateinit var homeIconButton: ButtonView
    private lateinit var homeIconItemButton: ItemButtonView
    private lateinit var actionButton: ButtonView
    private lateinit var errorMessage: TextView
    private lateinit var iconContainer: ViewContainer

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Add A New Home",
            size = 16,
        )

        homeNameButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}Click to set home name...",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}Click to set home name...",
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(homeNameButton)
                .alignTopToTopOf(homeNameButton)
                .alignBottomToBottomOf(homeNameButton)
                .margins(start = 50),
            background = Color.fromARGB(0x00000000),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    homeIconButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        size = 6,
                        text = "${ChatColor.GRAY}Select\nIcon...",
                        highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Select\nIcon..."
                    )

                    homeIconItemButton = addItemButtonView(
                        modifier = Modifier()
                            .size(55, 55)
                            .center(),
                        item = null,
                        visible = false,
                    )
                }
            }
        )

        actionButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(homeNameButton)
                .centerHorizontally()
                .margins(top = 75),
            text = "${ChatColor.GOLD}Save home",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Save home"
        )

        errorMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(actionButton)
                .centerHorizontally()
                .margins(top = 25),
            size = 4,
            alignment = Alignment.LEFT,
            visible = false,
            text = "${ChatColor.RED}Error: The dev was too lazy to figure out what went wrong...",
        )
    }

    override fun setUpdate(name: String?, icon: Material?) {
        if (name != null) homeNameButton.updateText(name)
        setIcon(icon)
    }

    override fun addHomeIconListener(listener: Listener) {
        homeIconButton.addListener(listener)
        homeIconItemButton.addListener(listener)
    }

    override fun setIcon(icon: Material?) {
        if (icon == null) {
            homeIconButton.visible = true
            homeIconItemButton.visible = false
            updateTextDisplay(homeIconButton)
            updateTextDisplay(homeIconItemButton)
        } else {
            homeIconButton.visible = false
            homeIconItemButton.visible = true
            homeIconItemButton.item = ItemStack(icon)
            updateTextDisplay(homeIconButton)
            updateTextDisplay(homeIconItemButton)
        }
    }

    override fun addTextListener(listener: TextListener) {
        homeNameButton.addTextChangedListener(listener)
    }

    override fun addActionListener(listener: Listener) {
        actionButton.addListener(listener)
    }

    override fun showError() {
        errorMessage.visible = true
        errorMessage.render()
    }
}

interface AddHomePresenter: Presenter {

    fun addHomeIconListener(listener: Listener)

    fun setIcon(icon: Material?)

    fun addTextListener(listener: TextListener)

    fun addActionListener(listener: Listener)

    fun showError()

    fun setUpdate(name: String?, icon: Material?)
}

class AddHomeInteractor(
    private val presenter: AddHomePresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val player: Player,
    private val homesRepository: HomesRepository,
): Interactor(presenter) {

    private var homeBuilder = HomeModel.Builder()

    override fun onCreate() {
        super.onCreate()

        val editor = homesRepository.getUpdateBuilder()
        if (editor != null) {
            homeBuilder = editor
            presenter.setUpdate(editor.name, editor.icon)
        }

        presenter.addTextListener(object : TextListener {
            override fun invoke(text: String) {
                homeBuilder.name(text)
            }
        })

        presenter.addHomeIconListener(object : Listener {
            override fun invoke() {
                routeTo(iconSelectionBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                        presenter.setIcon(icon?.type)
                        homeBuilder.icon(icon?.type)
                    }
                })
            }
        })

        presenter.addActionListener(object : Listener {
            override fun invoke() {
                val homeLocation = player.location.clone()
                homeLocation.yaw = 0f
                homeLocation.pitch = 0f

                val home = homeBuilder.location(homeLocation)
                    .build()

                if (home != null) {
                    homesRepository.saveHome(player, home)
                    routeBack()
                } else {
                    presenter.showError()
                }
            }
        })
    }

    override fun onClose() {
        super.onClose()
        homesRepository.updateHome(null)
    }
}