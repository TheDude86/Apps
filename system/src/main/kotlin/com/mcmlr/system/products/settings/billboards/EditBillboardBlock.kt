package com.mcmlr.system.products.settings.billboards

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.Environment
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
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.S
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.settings.billboards.SelectDefaultAppBlock.Companion.DEFAULT_APP_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class EditBillboardBlock @Inject constructor(
    player: Player,
    origin: Origin,
    selectDefaultAppBlock: SelectDefaultAppBlock,
    systemConfigRepository: SystemConfigRepository,
    applicationsRepository: ApplicationsRepository,
): Block(player, origin) {
    private val view = EditBillboardViewController(player, origin)
    private val interactor = EditBillboardInteractor(player, view, selectDefaultAppBlock, systemConfigRepository, applicationsRepository)

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
    private lateinit var xButton: TextInputView
    private lateinit var yButton: TextInputView
    private lateinit var zButton: TextInputView
    private lateinit var scaleButton: TextInputView
    private lateinit var defaultAppButton: ButtonView
    private lateinit var clearAppButton: ButtonView
    private lateinit var fixedButton: ButtonView
    private lateinit var rotationButton: TextInputView
    private lateinit var saveButton: ButtonView
    private lateinit var deleteButton: ButtonView
    private lateinit var errorMessage: TextView

    override fun setNameListener(listener: TextListener) = billboardNameButton.addTextChangedListener(listener)

    override fun setXListener(listener: TextListener) = xButton.addTextChangedListener(listener)

    override fun setYListener(listener: TextListener) = yButton.addTextChangedListener(listener)

    override fun setZListener(listener: TextListener) = zButton.addTextChangedListener(listener)

    override fun setScaleListener(listener: TextListener) = scaleButton.addTextChangedListener(listener)

    override fun setFixedListener(listener: Listener) = fixedButton.addListener(listener)

    override fun setRotationListener(listener: TextListener) = rotationButton.addTextChangedListener(listener)

    override fun setSaveListener(listener: Listener) = saveButton.addListener(listener)

    override fun setDeleteListener(listener: Listener) = deleteButton.addListener(listener)

    override fun setDefaultAppListener(listener: Listener) = defaultAppButton.addListener(listener)

    override fun setClearAppListener(listener: Listener) = clearAppButton.addListener(listener)

    override fun setName(text: String) {
        billboardNameButton.update(text = text)
    }

    override fun setX(text: String) {
        xButton.update(text = text)
    }

    override fun setY(text: String) {
        yButton.update(text = text)
    }

    override fun setZ(text: String) {
        zButton.update(text = text)
    }

    override fun setScale(text: String) {
        scaleButton.update(text = text)
    }

    override fun setDefaultApp(app: Environment<*>?) {
        if (app != null) {
            defaultAppButton.update(text = "${ChatColor.BOLD}${app.name()}")
        } else {
            defaultAppButton.update(text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.GRAY}Select default app...")
        }
    }

    override fun setFixed(text: String, isFixed: Boolean) {
        fixedButton.update(text = text)
        rotationButton.update(visible = isFixed)
    }

    override fun setRotation(text: String) {
        rotationButton.update(text = text)
    }

    override fun setErrorMessage(text: String) {
        errorMessage.update(text = text)
    }

    override fun setBillboard(billboard: BillboardModel, defaultApp: Environment<*>?) {
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
        if (defaultApp != null) {
            defaultAppButton.update(text = "${ChatColor.BOLD}${defaultApp.name()}")
        } else {
            defaultAppButton.update(text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.GRAY}Select default app...")
        }

        if (billboard.fixed) {
            fixedButton.update(text = "Fixed Rotation ☑")
            rotationButton.update(text = "Rotation: ${ChatColor.GRAY}${"%.2f".format(billboard.rotation)}")
        } else {
            fixedButton.update(text = "Fixed Rotation ☐")
            rotationButton.update(visible = false, text = "Rotation: ${ChatColor.GRAY}${"%.2f".format(billboard.rotation)}")
        }

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

        xButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(iconContainer)
                .alignTopToBottomOf(billboardNameButton)
                .margins(top = 100),
            text = "",
        )

        yButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(xButton)
                .alignTopToBottomOf(xButton)
                .margins(top = 50),
            text = "",
        )

        zButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(yButton)
                .alignTopToBottomOf(yButton)
                .margins(top = 50),
            text = "",
        )

        scaleButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(zButton)
                .alignTopToBottomOf(zButton)
                .margins(top = 50),
            text = "",
        )

        defaultAppButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(scaleButton)
                .alignTopToBottomOf(scaleButton)
                .margins(top = 50),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.GRAY}Select default app...",
        )

        clearAppButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(defaultAppButton)
                .alignStartToEndOf(defaultAppButton)
                .alignBottomToBottomOf(defaultAppButton)
                .margins(start = 150),
            text = "${ChatColor.GOLD}Clear",
        )

        fixedButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(defaultAppButton)
                .alignTopToBottomOf(defaultAppButton)
                .margins(top = 50),
            text = "",
        )

        rotationButton = addTextInputView(
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
    fun setBillboard(billboard: BillboardModel, defaultApp: Environment<*>?)

    fun setNameListener(listener: TextListener)

    fun setXListener(listener: TextListener)

    fun setYListener(listener: TextListener)

    fun setZListener(listener: TextListener)

    fun setScaleListener(listener: TextListener)

    fun setFixedListener(listener: Listener)

    fun setRotationListener(listener: TextListener)

    fun setSaveListener(listener: Listener)

    fun setDeleteListener(listener: Listener)

    fun setDefaultAppListener(listener: Listener)

    fun setClearAppListener(listener: Listener)

    fun setName(text: String)

    fun setX(text: String)

    fun setY(text: String)

    fun setZ(text: String)

    fun setScale(text: String)

    fun setDefaultApp(app: Environment<*>?)

    fun setFixed(text: String, isFixed: Boolean)

    fun setRotation(text: String)

    fun setErrorMessage(text: String)
}

class EditBillboardInteractor(
    private val player: Player,
    private val presenter: EditBillboardPresenter,
    private val selectDefaultAppBlock: SelectDefaultAppBlock,
    private val systemConfigRepository: SystemConfigRepository,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {
    private lateinit var billboard: BillboardModel

    fun setEditingBillboard(billboard: BillboardModel) {
        this.billboard = billboard.clone()
    }

    override fun onCreate() {
        super.onCreate()


        presenter.setBillboard(billboard, applicationsRepository.getApp(billboard.defaultApp))

        presenter.setNameListener(object : TextListener {
            override fun invoke(text: String) {
                if (text.isBlank()) {
                    presenter.setErrorMessage("${ChatColor.RED}Billboards can not have blank names...")
                    return
                }

                billboard.name = text
                presenter.setName("${ChatColor.BOLD}${ChatColor.GRAY}$text")
            }
        })

        presenter.setXListener(object : TextListener {
            override fun invoke(text: String) {
                val x = text.toDoubleOrNull()
                if (x == null) {
                    presenter.setErrorMessage("${ChatColor.RED}Invalid X value...")
                    return
                }

                billboard.x = x
                presenter.setX("X: ${ChatColor.GRAY}${"%.2f".format(billboard.x)}")
            }
        })

        presenter.setYListener(object : TextListener {
            override fun invoke(text: String) {
                val y = text.toDoubleOrNull()
                if (y == null) {
                    presenter.setErrorMessage("${ChatColor.RED}Invalid Y value...")
                    return
                }

                billboard.y = y
                presenter.setY("Y: ${ChatColor.GRAY}${"%.2f".format(billboard.y)}")
            }
        })

        presenter.setZListener(object : TextListener {
            override fun invoke(text: String) {
                val z = text.toDoubleOrNull()
                if (z == null) {
                    presenter.setErrorMessage("${ChatColor.RED}Invalid Z value...")
                    return
                }

                billboard.z = z
                presenter.setZ("Z: ${ChatColor.GRAY}${"%.2f".format(billboard.z)}")
            }
        })

        presenter.setScaleListener(object : TextListener {
            override fun invoke(text: String) {
                val scale = text.toIntOrNull()
                if (scale == null) {
                    presenter.setErrorMessage("${ChatColor.RED}Invalid Scale value...")
                    return
                }

                billboard.scale = scale
                presenter.setScale("Scale: ${ChatColor.GRAY}${billboard.scale}")
            }
        })

        presenter.setDefaultAppListener(object : Listener {
            override fun invoke() {
                routeTo(selectDefaultAppBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val defaultApp = bundle.getData<Environment<*>>(DEFAULT_APP_BUNDLE_KEY) ?: return

                        billboard.defaultApp = defaultApp.name().lowercase()
                        presenter.setDefaultApp(defaultApp)
                    }
                })
            }
        })

        presenter.setClearAppListener(object : Listener {
            override fun invoke() {
                billboard.defaultApp = null
                presenter.setDefaultApp(null)
            }
        })

        presenter.setFixedListener(object : Listener {
            override fun invoke() {
                billboard.fixed = !billboard.fixed
                presenter.setFixed("Fixed Rotation ${if (billboard.fixed) "☑" else "☐"}", billboard.fixed)
            }
        })

        presenter.setRotationListener(object : TextListener {
            override fun invoke(text: String) {
                val rotation = text.toFloatOrNull()
                if (rotation == null) {
                    presenter.setErrorMessage("${ChatColor.RED}Invalid Rotation value...")
                    return
                }

                billboard.rotation = rotation
                presenter.setRotation("Rotation: ${ChatColor.GRAY}${"%.2f".format(billboard.rotation)}")
            }
        })

        presenter.setSaveListener(object : Listener {
            override fun invoke() {
                systemConfigRepository.editBillboard(billboard)
                routeBack()
            }
        })

        presenter.setDeleteListener(object : Listener {
            override fun invoke() {
                systemConfigRepository.removeBillboard(billboard)
                routeBack()
            }
        })
    }
}