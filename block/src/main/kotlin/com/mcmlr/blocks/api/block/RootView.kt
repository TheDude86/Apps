package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.bolden
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class RootView(
    private val player: Player,
    private val origin: Origin,
    override var offset: Int = 0,
): Viewable {
    companion object {
        const val ITEM_VIEW_MULTIPLIER = 0.00025f
    }

    private val debug = false
    private val corners: MutableList<BlockDisplay> = mutableListOf()
    private val buttonMap = HashMap<Int, ClickableView>()
    private val scrollMap = HashMap<Int, FeedView>()
    private val measurements: ServerMeasurementConstants = getMeasurements()

    override fun render() { }

    override fun getDimensions(): Dimensions = Dimensions(1920, 1080)

    override fun getAbsolutePosition(): Coordinates = Coordinates(0, 0)

    override fun getPosition(): Coordinates = Coordinates(0, 0)

    override fun getViewModifier(): Modifier = Modifier()

    override fun start(): Int = -1920

    override fun top(): Int = 1080

    override fun end(): Int = 1920

    override fun bottom(): Int = -1080

    override fun clear() {
        buttonMap.clear()
        scrollMap.clear()
        corners.forEach { it.remove() }
    }

    override fun level(): Int = 0

    override fun updateDisplay() {}

    override fun addDestroyListener(listener: Listener) {}

    override fun addDependant(viewable: Viewable) {}

    override fun updatePosition() {}

    override fun updatePosition(x: Int?, y: Int?) {}

    override fun setScrolling(isScrolling: Boolean) {}

    override fun setTextInput(getInput: Boolean) {}

    override fun scroll(scrollEvent: ScrollEvent) {}

    override fun updateLocation(location: Location) {}

    override fun getLocation(): Location? = null

    override fun setFocus(view: Viewable) {}

    override fun updateFocus(view: Viewable) {}

    override fun player(): Player = player

    fun cursorEventV2(position: Coordinates, event: CursorEvent) {
        when(event) {
            CursorEvent.MOVE -> updateV2(position)
            CursorEvent.CLICK -> click()
            CursorEvent.CLEAR, CursorEvent.CALIBRATE -> { } //Do nothing
        }
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorEvent) {
        when(event) {
            CursorEvent.MOVE -> update(displays, cursor)
            CursorEvent.CLICK -> click()
            CursorEvent.CLEAR, CursorEvent.CALIBRATE -> { } //Do nothing
        }
    }

    private fun updateV2(position: Coordinates) {
        val x = buttonMap.values.mapNotNull {
            val display = it.dudeDisplay ?: return@mapNotNull null
            val view = (it as? Viewable) ?: return@mapNotNull null
            val dimensions = view.getDimensions()

            val cursorLocation = getDisplayLocation(position.x, position.y, it.level())
            val horizontalDistance =
                sqrt((display.location.x - cursorLocation.x).pow(2) + (display.location.y - cursorLocation.y).pow(2))
            val verticalDistance = abs(display.location.y - cursorLocation.y)
            val inBounds = horizontalDistance < (dimensions.width / 2000f) && verticalDistance < (dimensions.height / 7000f)

            if (inBounds) {
                val distance = sqrt(horizontalDistance.pow(2) + verticalDistance.pow(2))
                Pair(it, distance)
            } else {
                null
            }
        }.minByOrNull { it.second }


        buttonMap.values.forEach {
            val display = it.dudeDisplay ?: return@forEach
            when (it) {
                is ItemButtonView -> updateItemButton(it, display.uniqueId == x?.first?.dudeDisplay?.uniqueId)

                is ButtonView -> updateButton(it, display.uniqueId == x?.first?.dudeDisplay?.uniqueId)

                is ViewContainer -> updateContainerButton(it, display.uniqueId == x?.first?.dudeDisplay?.uniqueId)
            }
        }
    }

    private fun update(displays: List<Entity>, cursor: Location) {
        var button: DudeDisplay? = null
        var d = 1.0
        buttonMap.values.forEach {
            val display = it.dudeDisplay ?: return@forEach
            val pos = display.pos
            val dx = abs(pos.x - cursor.x)
            val dy = abs(pos.y - cursor.y)
            val dz = abs(pos.z - cursor.z)

            if (dx <= 0.09 && dy <= 0.04 && dz <= 0.09) {
                val horizontal = sqrt(dx.pow(2) + dz.pow(2))
                val distance = sqrt(horizontal.pow(2) + dy.pow(2))
                if (d > distance) {
                    button = it.dudeDisplay
                    d = distance
                }
            }
        }

        buttonMap.values.forEach {
            val display = it.dudeDisplay ?: return@forEach
            when (it) {
                is ItemButtonView -> updateItemButton(it, display.uniqueId == button?.uniqueId)

                is ButtonView -> updateButton(it, display.uniqueId == button?.uniqueId)

                is ViewContainer -> updateContainerButton(it, display.uniqueId == button?.uniqueId)
            }
        }


        var feed: DudeDisplay? = null
        d = 1.0
        scrollMap.values.forEach {
            val display = it.dudeDisplay ?: return@forEach
            val pos = display.pos
            val dx = abs(pos.x - cursor.x)
            val dy = abs(pos.y - cursor.y)
            val dz = abs(pos.z - cursor.z)

            if (dx <= 0.09 && dy <= 0.04 && dz <= 0.09) {
                val horizontal = sqrt(dx.pow(2) + dz.pow(2))
                val distance = sqrt(horizontal.pow(2) + dy.pow(2))
                if (d > distance) {
                    feed = it.dudeDisplay
                    d = distance
                }
            }
        }

        scrollMap.values.forEach {
            it.highlighted(it.dudeDisplay?.uniqueId == feed?.uniqueId)
        }
    }

    private fun updateItemButton(itemButtonView: ItemButtonView, highlighted: Boolean) {
        if (highlighted) {
            val dimensions = itemButtonView.getDimensions()
            itemButtonView.setSize(dimensions.width * 1.2f, dimensions.height * 1.2f)
            itemButtonView.highlighted = true
        } else {
            val dimensions = itemButtonView.getDimensions()
            itemButtonView.setSize(dimensions.width.toFloat(), dimensions.height.toFloat())
            itemButtonView.highlighted = false
        }

        itemButtonView.dudeDisplay?.renderUpdate()
    }

    private fun updateContainerButton(viewContainer: ViewContainer, highlighted: Boolean) {
        if (highlighted) {
            viewContainer.dudeDisplay?.setBackgroundColor(viewContainer.backgroundHighlight)
            viewContainer.highlighted = true
        } else {
            viewContainer.dudeDisplay?.setBackgroundColor(viewContainer.background)
            viewContainer.highlighted = false
        }

        viewContainer.dudeDisplay?.renderUpdate()
    }

    private fun updateButton(buttonView: ButtonView, highlighted: Boolean) {
        if (highlighted) {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.highlightedText ?: buttonView.text.bolden()
            buttonView.highlighted = true
        } else {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.text
            buttonView.highlighted = false
        }

        buttonView.dudeDisplay?.renderUpdate()
    }

    private fun click() {
        val buttonModel = buttonMap.values.find { it.highlighted } ?: return
        if (buttonModel.visible) buttonModel.listeners.forEach { it.invoke() }
    }

    override fun addContainerDisplay(view: ViewContainer): TextDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.STONE)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = Display.TextDisplay(net.minecraft.world.entity.EntityType.TEXT_DISPLAY, (player.world as CraftWorld).handle)
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        display.addTag("mcmlr.apps")
        display.text = Component.literal(".....")
        display.textOpacity = 4.toByte()
        display.entityData.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, view.background.asARGB())

        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(
                measurements.containerXOffset * dimensions.width,
                measurements.containerYOffset * dimensions.height,
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.containerWidth * dimensions.width,
                measurements.containerHeight * dimensions.height,
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        if (view is FeedView) scrollMap[display.id] = view
        if (view.clickable) buttonMap[display.id] = view

        render(display)
        return TextDudeDisplay(display, player)
    }

    override fun addTextDisplay(view: TextView): TextDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.DIRT)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val textSize = 0.04f * (view.size / 10.0f)
        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = Display.TextDisplay(net.minecraft.world.entity.EntityType.TEXT_DISPLAY, (player.world as CraftWorld).handle)
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        display.addTag("mcmlr.apps")
        display.text = Component.literal(view.text)
        display.entityData.set<Int>(Display.TextDisplay.DATA_LINE_WIDTH_ID, view.lineWidth)
        display.entityData.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, view.background.asARGB())

        var flags = display.flags.toInt()
        flags = flags and 0b11111100

        val alignmentBits = when (view.alignment) {
            Alignment.LEFT -> flags or 0b00001000
            Alignment.CENTER -> flags and 0b11100111
            Alignment.RIGHT -> flags or 0b00010000
        }

        display.flags = alignmentBits.toByte()

        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, view.teleportDuration)

        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(
                0f,
                measurements.textYOffset * (dimensions.height / (2 + (dimensions.height * 0.0005f))),
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(textSize, textSize, textSize),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        if (view is ButtonView) buttonMap[display.id] = view

        render(display)
        return TextDudeDisplay(display, player)
    }

    override fun updateContainerDisplay(view: ViewContainer) {
        view.clearCorners()
        showCorners(view, Material.STONE)

        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val display = (view.dudeDisplay as? TextDudeDisplay) ?: return

        display.teleport(player, getDisplayLocation(pos.x, pos.y, view.level()))
        display.setBackgroundColor(view.background)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(
                measurements.containerXOffset * dimensions.width,
                measurements.containerYOffset * dimensions.height,
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.containerWidth * dimensions.width,
                measurements.containerHeight * dimensions.height,
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        if (view.clickable) buttonMap[display.uniqueId] = view
        view.dudeDisplay?.renderUpdate()
    }

    override fun updateTextDisplay(view: TextView) {
        view.clearCorners()
        showCorners(view, Material.DIRT)
        if (!view.visible) {
            view.clear()
            return
        } else if (view.dudeDisplay == null) {
            view.render()
            return
        }

        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val textSize = 0.04f * (view.size / 10.0f)
        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = (view.dudeDisplay as? TextDudeDisplay) ?: return
        display.teleport(player, location)

        display.text = view.text
        display.setLineWidth(view.lineWidth)
        display.setBackgroundColor(view.background)
        display.setAlignment(view.alignment)
        display.setTeleportDuration(view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(
                0f,
                measurements.textYOffset * (dimensions.height / 2),
                0f
            ),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(textSize, textSize, textSize),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        view.dudeDisplay?.renderUpdate()
    }

    override fun addItemDisplay(view: ItemView): ItemDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.SAND)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = Display.ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY, (player.world as CraftWorld).handle)
        display.addTag("mcmlr.apps")
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        display.itemStack = CraftItemStack.asNMSCopy(view.item)
        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(0f, getItemYOffset(view.item.type, dimensions.height), 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        render(display)
        return ItemDudeDisplay(display, player)
    }

    override fun addItemDisplay(view: ItemButtonView): ItemDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.QUARTZ_BLOCK)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = Display.ItemDisplay(net.minecraft.world.entity.EntityType.ITEM_DISPLAY, (player.world as CraftWorld).handle)
        display.addTag("mcmlr.apps")
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        view.item?.let { display.itemStack = CraftItemStack.asNMSCopy(it) }
        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(0f, getItemYOffset(view.item?.type, dimensions.height), 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        buttonMap[display.id] = view

        render(display)
        return ItemDudeDisplay(display, player)
    }

    override fun updateItemDisplay(view: ItemView) {
        view.clearCorners()
        showCorners(view, Material.SAND)
        if (!view.visible) {
            view.clear()
            return
        } else if (view.dudeDisplay == null) {
            view.render()
            return
        }

        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = (view.dudeDisplay as? ItemDudeDisplay) ?: return
        display.teleport(player, location)

        display.itemStack = CraftItemStack.asNMSCopy(ItemStack(view.item))
        display.setTeleportDuration(view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(0f, getItemYOffset(view.item.type, dimensions.height), 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        display.renderUpdate()
    }

    override fun updateItemDisplay(view: ItemButtonView) {
        view.clearCorners()
        showCorners(view, Material.QUARTZ_BLOCK)
        if (!view.visible) {
            view.clear()
            return
        } else if (view.dudeDisplay == null) {
            view.render()
            return
        }

        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = (view.dudeDisplay as? ItemDudeDisplay) ?: return
        display.teleport(player, location)

        view.item?.let { display.itemStack = CraftItemStack.asNMSCopy(ItemStack(it)) }
        if (checkVersion(Versions.V1_20_2)) display.setTeleportDuration(view.teleportDuration)
        display.setTransformation(com.mojang.math.Transformation(
            Vector3f(0f, getItemYOffset(view.item?.type, dimensions.height), 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width,
            ),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        display.renderUpdate()
    }

    private fun getItemYOffset(item: Material?, height: Int) = when(item) {
        Material.PLAYER_HEAD -> (height - 10) * 0.0001f //TODO: Account for size difference
        else -> 0f
    }

    private fun getDisplayLocation(x: Int, y: Int, level: Int): Location {
        val xVector = xVector(origin.location().direction.normalize())
        val yVector = yVector(origin.location().direction.normalize())
        val direction = origin.location().direction.normalize()
        val location = origin.location().clone().subtract(direction.multiply(0.004 * level)).add(yVector.multiply(y / 8000.toDouble())).subtract(xVector.multiply(x / 8000.toDouble()))
        location.yaw += 180
        location.pitch *= -1
        return location
    }

    private fun showCorners(view: View, material: Material) {
        if (!debug) return
        val size = 0.001f

        val corners = listOf(
            Coordinates(view.start(), view.top()).offset(view.parent.getAbsolutePosition()),
            Coordinates(view.start(), view.bottom()).offset(view.parent.getAbsolutePosition()),
            Coordinates(view.end(), view.top()).offset(view.parent.getAbsolutePosition()),
            Coordinates(view.end(), view.bottom()).offset(view.parent.getAbsolutePosition()),
        )

        val cornerDisplays = corners.map {
            val location = getDisplayLocation(it.x, it.y, view.level())
            val display = player.world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
            display.addScoreboardTag("mcmlr.apps")
            display.block = material.createBlockData()
            display.transformation = Transformation(
                Vector3f(size / -2f, size / -2f, -size),
                AxisAngle4f(0f, 0f, 0f, 1f),
                Vector3f(size, size, size * 5f),
                AxisAngle4f(0f, 0f, 0f, 1f)
            )

            display
        }

        view.addCorners(cornerDisplays)
    }

    private fun render(display: Display) {
        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection

        playerConnection.send(ClientboundAddEntityPacket(
            display.id,
            display.getUUID(),
            display.x,
            display.y,
            display.z,
            display.xRot,
            display.yRot,
            display.type,
            0,
            display.deltaMovement,
            display.yHeadRot.toDouble())
        )

        playerConnection.send(
            ClientboundSetEntityDataPacket(
                display.id,
                display.getEntityData().nonDefaultValues
            )
        )
    }

    private fun xVector(direction: Vector): Vector = Vector(direction.z, 0.0, -direction.x)

    private fun yVector(direction: Vector): Vector {
        val xHypotenuse = sqrt(direction.x.pow(2) + direction.z.pow(2))
        return Vector(-direction.y * (direction.x / xHypotenuse), xHypotenuse, -direction.y * (direction.z / xHypotenuse))
    }
}