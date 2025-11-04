package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.folia.FoliaFactory
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.UUID

class EntityDudeDisplay(
    val entity: LivingEntity,
): DudeDisplay(entityDisplay = entity)

class BlockDudeDisplay(
    val display: BlockDisplay,
): DudeDisplay(blockDisplay = display)

class ItemDudeDisplay(
    val display: ItemDisplay,
): DudeDisplay(itemDisplay = display)

class TextDudeDisplay(
    var display: TextDisplay,
): DudeDisplay(textDisplay = display) {
    var text: String?
        get() = display.text
        set(value) { display.text = value }
}

abstract class DudeDisplay(
    private val textDisplay: TextDisplay? = null,
    private val itemDisplay: ItemDisplay? = null,
    private val blockDisplay: BlockDisplay? = null,
    private val entityDisplay: LivingEntity? = null,
) {
    val uniqueId: UUID =
        textDisplay?.uniqueId ?:
        itemDisplay?.uniqueId ?:
        blockDisplay?.uniqueId ?:
        entityDisplay?.uniqueId ?:
        throw Exception("Bruh... how")

    var transformation: Transformation
        get() = textDisplay?.transformation ?: itemDisplay?.transformation ?: blockDisplay?.transformation ?: Transformation(Vector3f(0f, 0f, 0f), AxisAngle4f(0f, 0f, 0f ,1f), Vector3f(0f, 0f, 0f), AxisAngle4f(0f, 0f, 0f, 1f))
        set(value) {
            textDisplay?.transformation = value
            itemDisplay?.transformation = value
            blockDisplay?.transformation = value
        }

    val location: Location
        get() {
            return textDisplay?.location ?:
            itemDisplay?.location ?:
            blockDisplay?.location ?:
            entityDisplay?.location ?:
            throw Exception("Bruh... how")
        }

    fun scroll(event: ScrollEvent) {
        val direction = if (event == ScrollEvent.UP) -0.01 else 0.01
        if (checkVersion(Versions.V1_20_2)) {
            textDisplay?.teleportDuration = 5
            itemDisplay?.teleportDuration = 5
            blockDisplay?.teleportDuration = 5
        }

        textDisplay?.let { FoliaFactory.teleport(it, it.location.add(0.0, direction, 0.0)) }
        itemDisplay?.let { FoliaFactory.teleport(it, it.location.add(0.0, direction, 0.0)) }
        blockDisplay?.let { FoliaFactory.teleport(it, it.location.add(0.0, direction, 0.0)) }
        entityDisplay?.let { FoliaFactory.teleport(it, it.location.add(0.0, direction, 0.0)) }
    }

    fun updateLocation(location: Location) {
        textDisplay?.let { FoliaFactory.teleport(it, location) }
        itemDisplay?.let { FoliaFactory.teleport(it, location) }
        blockDisplay?.let { FoliaFactory.teleport(it, location) }
        entityDisplay?.let { FoliaFactory.teleport(it, location) }
    }

    fun remove() {
        textDisplay?.remove()
        itemDisplay?.remove()
        blockDisplay?.remove()
        entityDisplay?.remove()
    }

    fun background(color: Color) {
        textDisplay?.backgroundColor = color
    }

    fun setTeleportDuration(duration: Int) {
        textDisplay?.teleportDuration = duration
        itemDisplay?.teleportDuration = duration
        blockDisplay?.teleportDuration = duration
    }
}
