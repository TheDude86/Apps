package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
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

class AddWarpsBlock @Inject constructor(
    player: Player,
    camera: Camera,
    private val iconSelectionBlock: IconSelectionBlock,
    private val warpsRepository: WarpsRepository,
): Block(player, camera) {
    private val view = AddWarpViewController(player, camera)
    private val interactor = AddWarpInteractor(view, iconSelectionBlock, player, warpsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AddWarpViewController(
    player: Player,
    camera: Camera,
): NavigationViewController(player, camera), AddWarpPresenter {

    private lateinit var warpNameButton: TextInputView
    private lateinit var warpIconButton: ButtonView
    private lateinit var warpIconItemButton: ItemButtonView
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Add A New Warp",
            size = 16,
        )

        warpNameButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}Click to set warp name...",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}Click to set warp name...",
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(warpNameButton)
                .alignTopToTopOf(warpNameButton)
                .alignBottomToBottomOf(warpNameButton)
                .margins(start = 50),
            background = Color.fromARGB(0x00000000),
        ) {
            warpIconButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .center(),
                size = 6,
                text = "${ChatColor.GRAY}Select\nIcon...",
                highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Select\nIcon..."
            )

            warpIconItemButton = addItemButtonView(
                modifier = Modifier()
                    .size(55, 55)
                    .center(),
                item = null,
                visible = false,
            )
        }

        actionButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(warpNameButton)
                .centerHorizontally()
                .margins(top = 75),
            text = "${ChatColor.GOLD}Save warp",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Save warp"
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
        if (name !=null) warpNameButton.updateText(name)
        setIcon(icon)
    }

    override fun addWarpIconListener(listener: () -> Unit) {
        warpIconButton.addListener(listener)
        warpIconItemButton.addListener(listener)
    }

    override fun setIcon(icon: Material?) {
        if (icon == null) {
            warpIconButton.visible = true
            warpIconItemButton.visible = false
            updateTextDisplay(warpIconButton)
            updateTextDisplay(warpIconItemButton)
        } else {
            warpIconButton.visible = false
            warpIconItemButton.visible = true
            warpIconItemButton.item = ItemStack(icon)
            updateTextDisplay(warpIconButton)
            updateTextDisplay(warpIconItemButton)
        }
    }

    override fun addTextListener(listener: (String) -> Unit) {
        warpNameButton.addTextChangedListener(listener)
    }

    override fun addActionListener(listener: () -> Unit) {
        actionButton.addListener(listener)
    }

    override fun showError() {
        errorMessage.visible = true
        errorMessage.render()
    }
}

interface AddWarpPresenter: Presenter {

    fun addWarpIconListener(listener: () -> Unit)

    fun setIcon(icon: Material?)

    fun addTextListener(listener: (String) -> Unit)

    fun addActionListener(listener: () -> Unit)

    fun showError()

    fun setUpdate(name: String?, icon: Material?)
}

class AddWarpInteractor(
    private val presenter: AddWarpPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val player: Player,
    private val warpsRepository: WarpsRepository,
): Interactor(presenter) {

    private var homeBuilder = WarpModel.Builder()

    override fun onCreate() {
        super.onCreate()

        val editor = warpsRepository.getUpdateBuilder()
        if (editor != null) {
            homeBuilder = editor
            presenter.setUpdate(editor.name, editor.icon)
        }

        presenter.addTextListener {
            homeBuilder.name(it)
        }

        presenter.addWarpIconListener {
            routeTo(iconSelectionBlock) { bundle ->
                val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                presenter.setIcon(icon?.type)
                homeBuilder.icon(icon?.type)
            }
        }

        presenter.addActionListener {
            val homeLocation = player.location.clone()
            homeLocation.yaw = 0f
            homeLocation.pitch = 0f

            val home = homeBuilder.location(homeLocation)
                .build()

            if (home != null) {
                warpsRepository.saveWarp(home)
                routeBack()
            } else {
                presenter.showError()
            }
        }
    }

    override fun onClose() {
        super.onClose()
        warpsRepository.updateWarp(null)
    }
}
