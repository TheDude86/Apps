package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.isSpigotServer
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import com.mcmlr.folia.FoliaFactory
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class RootView(
    private val player: Player,
    private val origin: Location,
    override var offset: Int = 0,
): Viewable {
    companion object {
        const val ITEM_VIEW_MULTIPLIER = 0.00025f
    }

    private val debug = false
    private val corners: MutableList<BlockDisplay> = mutableListOf()
    private val buttonMap = HashMap<UUID, ClickableView>()
    private val scrollMap = HashMap<UUID, FeedView>()
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

    fun cursorEventV2(position: Coordinates, event: CursorEvent) {
        when(event) {
            CursorEvent.MOVE -> updateV2(position)
            CursorEvent.CLICK -> click()
            CursorEvent.CLEAR -> { } //Do nothing
        }
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorEvent) {
        when(event) {
            CursorEvent.MOVE -> update(displays, cursor)
            CursorEvent.CLICK -> click()
            CursorEvent.CLEAR -> { } //Do nothing
        }
    }

    private fun updateV2(position: Coordinates) {
//        log(Log.ERROR, "X=${position.x} Y=${position.y}")

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
        val button = displays
            .filter {
                buttonMap.containsKey(it.uniqueId)
            }
            .minByOrNull { cursor.distance(it.location) }

        buttonMap.values.forEach {
            val display = it.dudeDisplay ?: return@forEach
            when (it) {
                is ItemButtonView -> updateItemButton(it, display.uniqueId == button?.uniqueId)

                is ButtonView -> updateButton(it, display.uniqueId == button?.uniqueId)

                is ViewContainer -> updateContainerButton(it, display.uniqueId == button?.uniqueId)
            }
        }

        val feed = displays
            .filter {
                scrollMap.containsKey(it.uniqueId)
            }
            .minByOrNull { cursor.distance(it.location) }

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
    }

    private fun updateContainerButton(viewContainer: ViewContainer, highlighted: Boolean) {
        if (highlighted) {
            (viewContainer.dudeDisplay as? TextDudeDisplay)?.background(viewContainer.backgroundHighlight)
            viewContainer.highlighted = true
        } else {
            (viewContainer.dudeDisplay as? TextDudeDisplay)?.background(viewContainer.background)
            viewContainer.highlighted = false
        }
    }

    private fun updateButton(buttonView: ButtonView, highlighted: Boolean) {
        if (highlighted) {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.highlightedText ?: "${ChatColor.BOLD}${buttonView.text}"
            buttonView.highlighted = true
        } else {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.text
            buttonView.highlighted = false
        }
    }

    private fun click() {
        val buttonModel = buttonMap.values.find { it.highlighted } ?: return
        if (buttonModel.visible) buttonModel.listeners.forEach { it.invoke() }
    }

    override fun player(): Player = player

    override fun addContainerDisplay(view: ViewContainer): TextDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.STONE)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = player.world.spawn(location, TextDisplay::class.java)

        if (isSpigotServer()) FoliaFactory.teleport(display, location)

        display.addScoreboardTag("mcmlr.apps")
        display.text = "....."
        display.textOpacity = 4.toByte()
        display.backgroundColor = view.background
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(
                measurements.containerXOffset * dimensions.width,
                measurements.containerYOffset * dimensions.height,
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.containerWidth * dimensions.width,
                measurements.containerHeight * dimensions.height,
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )

        if (view is FeedView) scrollMap[display.uniqueId] = view
        if (view.clickable) buttonMap[display.uniqueId] = view
        return TextDudeDisplay(display)
    }

    override fun addTextDisplay(view: TextView): TextDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.DIRT)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val textSize = 0.04f * (view.size / 10.0f)
        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = player.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
        if (isSpigotServer()) FoliaFactory.teleport(display, location)

        display.addScoreboardTag("mcmlr.apps")
        display.text = view.text
        display.lineWidth = view.lineWidth
        display.backgroundColor = view.background
        display.alignment = view.alignment.textAlignment
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(
                0f,
                measurements.textYOffset * (dimensions.height / (2 + (dimensions.height * 0.0005f))),
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(textSize, textSize, textSize),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )

        if (view is ButtonView) buttonMap[display.uniqueId] = view
        return TextDudeDisplay(display)
    }

    override fun updateContainerDisplay(view: ViewContainer) {
        view.clearCorners()
        showCorners(view, Material.STONE)

        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        val display = (view.dudeDisplay as? TextDudeDisplay)?.display ?: return
        FoliaFactory.teleport(display, location)

        display.backgroundColor = view.background
        display.transformation = Transformation(
            Vector3f(
                measurements.containerXOffset * dimensions.width,
                measurements.containerYOffset * dimensions.height,
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.containerWidth * dimensions.width,
                measurements.containerHeight * dimensions.height,
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )

        if (view.clickable) buttonMap[display.uniqueId] = view
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
        val display = (view.dudeDisplay as? TextDudeDisplay)?.display ?: return
        FoliaFactory.teleport(display, location)

        display.text = view.text
        display.lineWidth = view.lineWidth
        display.backgroundColor = view.background
        display.alignment = view.alignment.textAlignment
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(
                0f,
                measurements.textYOffset * (dimensions.height / 2),
                0f
            ),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(textSize, textSize, textSize), AxisAngle4f(0f, 0f, 0f, 1f)
        )
    }

    override fun addEntityDisplay(view: EntityView): EntityDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.OBSIDIAN)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = player.world.spawnEntity(location, view.entity) as LivingEntity
        display.setAI(false)
        display.setGravity(false)
        display.getAttribute(Attribute.SCALE)?.baseValue = 0.001
        display.addScoreboardTag("mcmlr.apps")
        Attribute.SCALE

        if (isSpigotServer()) FoliaFactory.teleport(display, location)

        return EntityDudeDisplay(display)
    }

    override fun updateEntityDisplay(view: EntityView): EntityDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.OBSIDIAN)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        return null
    }

    override fun addItemDisplay(view: ItemView): ItemDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.SAND)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = player.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        display.addScoreboardTag("mcmlr.apps")
        if (isSpigotServer()) FoliaFactory.teleport(display, location)

        display.itemStack = view.item
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(0f, getItemYOffset(view.item.type, dimensions.height), 0f),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )

        return ItemDudeDisplay(display)
    }

    override fun addItemDisplay(view: ItemButtonView): ItemDudeDisplay? {
        if (!view.visible) return null
        showCorners(view, Material.QUARTZ_BLOCK)
        val pos = view.getPosition().offset(view.parent.getAbsolutePosition())
        val dimensions = view.getDimensions()

        val location = getDisplayLocation(pos.x, pos.y, view.level())
        location.yaw -= 180
        val display = player.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        display.addScoreboardTag("mcmlr.apps")
        if (isSpigotServer()) FoliaFactory.teleport(display, location)

        view.item?.let { display.itemStack = it }
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(0f, getItemYOffset(view.item?.type, dimensions.height), 0f),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )

        buttonMap[display.uniqueId] = view
        return ItemDudeDisplay(display)
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
        val display = (view.dudeDisplay as? ItemDudeDisplay)?.display ?: return
        FoliaFactory.teleport(display, location)

        display.itemStack = ItemStack(view.item)
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(0f, getItemYOffset(view.item.type, dimensions.height), 0f),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )
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
        val display = (view.dudeDisplay as? ItemDudeDisplay)?.display ?: return
        FoliaFactory.teleport(display, location)

        view.item?.let { display.itemStack = ItemStack(it) }
        if (checkVersion(Versions.V1_20_2)) display.teleportDuration = view.teleportDuration
        display.transformation = Transformation(
            Vector3f(0f, getItemYOffset(view.item?.type, dimensions.height), 0f),
            AxisAngle4f(0f, 0f, 0f, 1f),
            Vector3f(
                measurements.itemDimension * dimensions.width,
                measurements.itemDimension * dimensions.height,
                measurements.itemDimension * dimensions.width,
            ),
            AxisAngle4f(0f, 0f, 0f, 1f)
        )
    }

    private fun getItemYOffset(item: Material?, height: Int) = when(item) {
        Material.PLAYER_HEAD -> (height - 10) * 0.0001f //TODO: Account for size difference
        else -> 0f
    }

    private fun getDisplayLocation(x: Int, y: Int, level: Int): Location {
        val xVector = xVector(origin.direction.normalize())
        val yVector = yVector(origin.direction.normalize())
        val direction = origin.direction.normalize()
        val location = origin.clone().subtract(direction.multiply(0.004 * level)).add(yVector.multiply(y / 8000.toDouble())).subtract(xVector.multiply(x / 8000.toDouble()))
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

    private fun xVector(direction: Vector): Vector = Vector(direction.z, 0.0, -direction.x)

    private fun yVector(direction: Vector): Vector {
        val xHypotenuse = sqrt(direction.x.pow(2) + direction.z.pow(2))
        return Vector(-direction.y * (direction.x / xHypotenuse), xHypotenuse, -direction.y * (direction.z / xHypotenuse))
    }
}