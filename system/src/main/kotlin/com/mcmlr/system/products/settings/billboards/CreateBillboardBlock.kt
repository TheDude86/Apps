package com.mcmlr.system.products.settings.billboards

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.CursorEventListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.packets.ItemDisplayPacket
import com.mcmlr.blocks.api.packets.TextDisplayPacket
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import com.mcmlr.system.S
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.minetunes.player.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import java.util.UUID
import javax.inject.Inject
import kotlin.math.cos
import kotlin.time.Duration.Companion.milliseconds

class CreateBillboardBlock @Inject constructor(
    player: Player,
    origin: Origin,
    iconSelectionBlock: IconSelectionBlock,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {
    private val view = CreateBillboardViewController(player, origin)
    private val interactor = CreateBillboardInteractor(player, view, iconSelectionBlock, systemConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class CreateBillboardViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), CreateBillboardPresenter {

    private lateinit var billboardNameButton: TextInputView
    private lateinit var billboardIconButton: ButtonView
    private lateinit var billboardIconItemButton: ItemButtonView
    private lateinit var iconContainer: ViewContainer
    private lateinit var createButton: ButtonView
    private lateinit var cancelButton: ButtonView

    override fun setEditingBillboard(playlist: Playlist) {
        val name = playlist.name
        val iconData = playlist.icon?.data
        if (name != null) billboardNameButton.update(text = name)
        if (iconData != null) {
            billboardIconButton.visible = false
            billboardIconItemButton.visible = true
            billboardIconItemButton.item = ItemStack(Material.valueOf(iconData))
            updateTextDisplay(billboardIconButton)
            updateTextDisplay(billboardIconItemButton)
        }

        createButton.update(text = R.getString(player, S.CREATE_BILLBOARD_UPDATE_BUTTON.resource()))
    }

    override fun addCreateListener(listener: Listener) {
        createButton.addListener(listener)
    }

    override fun addCancelListener(listener: Listener) {
        cancelButton.addListener(listener)
    }

    override fun setBillboardNameText(name: String) {
        billboardNameButton.update(text = name)
    }

    override fun addBillboardNameTextListener(listener: TextListener) {
        billboardNameButton.addTextChangedListener(listener)
    }

    override fun addBillboardIconListener(listener: Listener) {
        billboardIconButton.addListener(listener)
        billboardIconItemButton.addListener(listener)
    }

    override fun addRouteBackListener(listener: Listener) {
        backButton?.addListener(listener)
    }

    override fun setIcon(icon: Material?) {
        if (icon == null) {
            billboardIconButton.visible = true
            billboardIconItemButton.visible = false
            updateTextDisplay(billboardIconButton)
            updateTextDisplay(billboardIconItemButton)
        } else {
            billboardIconButton.visible = false
            billboardIconItemButton.visible = true
            billboardIconItemButton.update(item = ItemStack(icon))
            updateTextDisplay(billboardIconButton)
            updateTextDisplay(billboardIconItemButton)
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
            text = R.getString(player, S.CREATE_BILLBOARD_TITLE.resource()),
            size = 16,
        )

        billboardNameButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}${R.getString(player, S.CREATE_BILLBOARD_NAME_PLACEHOLDER.resource())}",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}${R.getString(player, S.CREATE_BILLBOARD_NAME_PLACEHOLDER.resource())}",
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(billboardNameButton)
                .alignTopToTopOf(billboardNameButton)
                .alignBottomToBottomOf(billboardNameButton)
                .margins(end = 150),
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

        createButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(billboardNameButton)
                .x(200)
                .margins(top = 100),
            text = R.getString(player, S.CREATE_BILLBOARD_CREATE_BUTTON.resource())
        )

        cancelButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(billboardNameButton)
                .x(-200)
                .margins(top = 100),
            text = R.getString(player, S.CREATE_BILLBOARD_CANCEL_BUTTON.resource())
        )
    }

}

interface CreateBillboardPresenter: Presenter {
    fun addBillboardNameTextListener(listener: TextListener)

    fun setBillboardNameText(name: String)

    fun addBillboardIconListener(listener: Listener)

    fun setIcon(icon: Material?)

    fun addCreateListener(listener: Listener)

    fun addCancelListener(listener: Listener)

    fun setEditingBillboard(playlist: Playlist)

    fun addRouteBackListener(listener: Listener)
}

class CreateBillboardInteractor(
    private val player: Player,
    private val presenter: CreateBillboardPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {

    private var billboardName: String? = null
    private var billboardIcon: Material? = null
    private var minimized = false

    override fun onResume(newOrigin: Location?) {
        super.onResume(newOrigin)

        if (minimized) {
            minimize()
            return
        }
    }

    override fun onCreate() {
        super.onCreate()

        billboardName?.let {
            presenter.setBillboardNameText(it)
        }

        presenter.addBillboardNameTextListener(listener = object : TextListener {
            override fun invoke(text: String) {
                billboardName = text
            }
        })

        presenter.addBillboardIconListener(object : Listener {
            override fun invoke() {
                routeTo(iconSelectionBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                        presenter.setIcon(icon?.type)
                        billboardIcon = icon?.type
                    }
                })
            }
        })

        presenter.addCreateListener(object : Listener {
            override fun invoke() {
                val name = billboardName
                val icon = billboardIcon
                var selectedLocation: Location? = null

                minimized = true
                minimize()
                val hologram = TextDisplayPacket(
                    players = listOf(player),
                    location = player.eyeLocation.block.location,
                    background = Color.fromARGB(195, 0, 220, 0),
                    text = "........",
                    opacity = 4,
                )

                val bar = ItemDisplayPacket(
                    players = listOf(player),
                    location = player.eyeLocation.block.location,
                    item = ItemStack(Material.LIME_CONCRETE),
                    scale = Vector3f(0.05f, 1f, 0.05f)
                )

                val job = CoroutineScope(Dispatchers.IO).launch {
                    delay(10.milliseconds)
                    addCursorEventListener(object : CursorEventListener {
                        override fun invoke(cursor: Location, event: CursorModel) {
                            if (event.event == CursorEvent.CLICK) close()
                        }
                    })

                    var rotation = 0f
                    var offset = 0.0

                    while (true) {
                        val o = cos(offset - 1.0) * 0.1
                        selectedLocation = player.getTargetBlock(null, 20).location.clone().add(0.5, 1.75 + o, 0.5)
                        selectedLocation.yaw = rotation

                        bar.update(location = selectedLocation)

                        selectedLocation.yaw = player.location.yaw - 180
                        selectedLocation.add(0.0, 0.75, 0.0)
                        hologram.update(location = selectedLocation)

                        rotation = (rotation + 3f) % 360f
                        offset = (offset + 0.1)
                        delay(50.milliseconds)
                    }
                }

                job.disposeOn(disposer = this@CreateBillboardInteractor)
                job.invokeOnCompletion {
                    hologram.remove()
                    bar.remove()

                    systemConfigRepository.addBillboard(
                        BillboardModel(
                            id = UUID.randomUUID(),
                            name = name ?: "Billboard #${systemConfigRepository.model.billboards.billboards.size}",
                            icon = icon?.name,
                            world = selectedLocation?.world?.name ?: "",
                            x = selectedLocation?.x ?: 0.0,
                            y = selectedLocation?.y ?: 0.0,
                            z = selectedLocation?.z ?: 0.0,
                            rotation = selectedLocation?.yaw ?: 0f,
                            scale = 10,
                            fixed = false,
                            defaultApp = null,
                        )
                    )
                    close()
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

            }
        })
    }
}
