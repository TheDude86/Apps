package com.mcmlr.system.products.settings.billboards

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.S
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class EditBillboardBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    private val view = EditBillboardViewController(player, origin)
    private val interactor = EditBillboardInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setBillboard(billboard: BillboardModel) {
        interactor.setEditingBillboard(billboard)
    }
}

class EditBillboardViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), EditBillboardPresenter {
    private lateinit var billboardNameButton: TextInputView
    private lateinit var billboardIconButton: ButtonView
    private lateinit var billboardIconItemButton: ItemButtonView
    private lateinit var iconContainer: ViewContainer
    private lateinit var xButton: ButtonView
    private lateinit var yButton: ButtonView
    private lateinit var zButton: ButtonView
    private lateinit var scaleButton: ButtonView
    private lateinit var fixedButton: ButtonView
    private lateinit var rotationButton: ButtonView
    private lateinit var saveButton: ButtonView
    private lateinit var deleteButton: ButtonView

    override fun setBillboard(billboard: BillboardModel) {
        val icon = billboard.icon
        if (icon != null) {
            val icon = Material.valueOf(icon)
            billboardIconButton.update(visible = false)
            billboardIconItemButton.update(visible = true, item = ItemStack(icon))
        } else {
            billboardIconButton.update(visible = true)
            billboardIconItemButton.update(visible = false, item = ItemStack(Material.AIR))
        }

        billboardNameButton.update(text = "${ChatColor.GRAY}${ChatColor.BOLD}${ChatColor.ITALIC}${billboard.name}")
        xButton.update(text = "X: ${ChatColor.GRAY}${"%.2f".format(billboard.x)}")
        yButton.update(text = "Y: ${ChatColor.GRAY}${"%.2f".format(billboard.y)}")
        zButton.update(text = "Z: ${ChatColor.GRAY}${"%.2f".format(billboard.z)}")

        fixedButton.update(text = "Fixed Rotation ${if (billboard.fixed) "☐" else "☑"}")
        rotationButton.update(text = "Rotation: ${ChatColor.GRAY}${"%.2f".format(billboard.rotation)}")
        scaleButton.update(text = "Scale: ${ChatColor.GRAY}${billboard.scale}")
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Edit Billboard",
            size = 16,
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(title)
                .alignTopToBottomOf(title)
                .margins(top = 100),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    billboardIconButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        size = 6,
                        text = "${ChatColor.GRAY}${R.getString(player, S.CREATE_BILLBOARD_ICON.resource())}",
                        highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}${R.getString(player, S.CREATE_BILLBOARD_ICON.resource())}"
                    )

                    billboardIconItemButton = addItemButtonView(
                        modifier = Modifier()
                            .size(55, 55)
                            .center(),
                        item = null,
                        visible = false,
                    )
                }
            }
        )

        billboardNameButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(iconContainer)
                .alignBottomToBottomOf(iconContainer)
                .alignStartToEndOf(iconContainer)
                .margins(start = 100),
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}${R.getString(player, S.CREATE_BILLBOARD_NAME_PLACEHOLDER.resource())}",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}${R.getString(player, S.CREATE_BILLBOARD_NAME_PLACEHOLDER.resource())}",
        )

        xButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(iconContainer)
                .alignTopToBottomOf(billboardNameButton)
                .margins(top = 100),
            text = "",
        )

        yButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(xButton)
                .alignTopToBottomOf(xButton)
                .margins(top = 50),
            text = "",
        )

        zButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(yButton)
                .alignTopToBottomOf(yButton)
                .margins(top = 50),
            text = "",
        )

        scaleButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(zButton)
                .alignTopToBottomOf(zButton)
                .margins(top = 50),
            text = "",
        )

        fixedButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(scaleButton)
                .alignTopToBottomOf(scaleButton)
                .margins(top = 50),
            text = "",
        )

        rotationButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(fixedButton)
                .alignTopToBottomOf(fixedButton)
                .margins(top = 50),
            text = "",
        )

        saveButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(-300)
                .margins(bottom = 150),
            text = "${ChatColor.GOLD}Save",
        )

        deleteButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(300)
                .margins(bottom = 150),
            text = "${ChatColor.RED}Delete",
        )
    }

}

interface EditBillboardPresenter: Presenter {
    fun setBillboard(billboard: BillboardModel)
}

class EditBillboardInteractor(
    private val player: Player,
    private val presenter: EditBillboardPresenter,
): Interactor(presenter) {
    private lateinit var billboard: BillboardModel

    fun setEditingBillboard(billboard: BillboardModel) {
        this.billboard = billboard
    }

    override fun onCreate() {
        super.onCreate()
        presenter.setBillboard(billboard)
    }
}