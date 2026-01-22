package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.blocks.api.log
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
import org.bukkit.entity.Player
import kotlin.math.abs


class BlockDudeDisplay(
    val display: Display.BlockDisplay,
    player: Player,
): DudeDisplay(blockDisplay = display, player = player)

class ItemDudeDisplay(
    private val display: Display.ItemDisplay,
    player: Player,
): DudeDisplay(itemDisplay = display, player = player) {

    var itemStack: ItemStack
        get() = display.itemStack
        set(value) { display.itemStack = value }

}

class TextDudeDisplay(
    var display: Display.TextDisplay,
    player: Player,
): DudeDisplay(textDisplay = display, player = player) {
    var text: String?
        get() = display.text.string
        set(value) { display.text = Component.literal(value) }

    fun setLineWidth(lineWidth: Int) {
        display.entityData.set<Int>(Display.TextDisplay.DATA_LINE_WIDTH_ID, lineWidth)
    }
}

abstract class DudeDisplay(
    private val textDisplay: Display.TextDisplay? = null,
    private val itemDisplay: Display.ItemDisplay? = null,
    private val blockDisplay: Display.BlockDisplay? = null,
    private val player: Player,
) {
    companion object {
        const val DELTA_MINIMUM = 0.000244140625
    }

    val uniqueId: Int =
        textDisplay?.id ?:
        itemDisplay?.id ?:
        blockDisplay?.id ?:
        throw Exception("Bruh... how")

    val pos: Vec3
        get() {
            return textDisplay?.position() ?:
            itemDisplay?.position() ?:
            blockDisplay?.position() ?:
            throw Exception("Bruh... how")
        }

    val yaw: Float
        get() {
            return textDisplay?.yRot ?:
            itemDisplay?.yRot ?:
            blockDisplay?.yRot ?:
            throw Exception("Bruh... how")
        }

    val pitch: Float
        get() {
            return textDisplay?.xRot ?:
            itemDisplay?.xRot ?:
            blockDisplay?.xRot ?:
            throw Exception("Bruh... how")
        }

    fun setAlignment(alignment: Alignment) {
        val display = textDisplay ?: return

        var flags = display.flags.toInt()
        flags = flags and 0b11111100

        val alignmentBits = when (alignment) {
            Alignment.LEFT -> flags or 0b00001000
            Alignment.CENTER -> flags and 0b11100111
            Alignment.RIGHT -> flags or 0b00010000
        }

        display.flags = alignmentBits.toByte()
    }

    fun setBackgroundColor(color: Color) {
        textDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, color.asARGB())
        itemDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, color.asARGB())
        blockDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, color.asARGB())
    }

    fun setTransformation(transformation: com.mojang.math.Transformation) {
        textDisplay?.setTransformation(transformation)
        itemDisplay?.setTransformation(transformation)
        blockDisplay?.setTransformation(transformation)
    }

    fun teleport(player: Player, location: Location) {
        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        val p = pos
        val dx = location.x - p.x
        val dy = location.y - p.y
        val dz = location.z - p.z
        val nx = if (abs(dx) >= DELTA_MINIMUM) location.x else p.x
        val ny = if (abs(dy) >= DELTA_MINIMUM) location.y else p.y
        val nz = if (abs(dz) >= DELTA_MINIMUM) location.z else p.z
        val px = if (abs(dx) >= DELTA_MINIMUM) dx else 0.0
        val py = if (abs(dy) >= DELTA_MINIMUM) dy else 0.0
        val pz = if (abs(dz) >= DELTA_MINIMUM) dz else 0.0

        textDisplay?.setPos(nx, ny, nz)
        itemDisplay?.setPos(nx, ny, nz)
        blockDisplay?.setPos(nx, ny, nz)

        playerConnection.send(
            ClientboundMoveEntityPacket.PosRot(
                uniqueId,
                (px * 4096).toInt().toShort(),
                (py * 4096).toInt().toShort(),
                (pz * 4096).toInt().toShort(),
                ((location.yaw * 256.0f) / 360.0f).toInt().toByte(),
                ((location.pitch * 256.0f) / 360.0f).toInt().toByte(),
                true
            )
        )
    }

    val location: Location
        get() {
            return convertToLocation(textDisplay) ?:
            convertToLocation(itemDisplay) ?:
            convertToLocation(blockDisplay) ?:
            throw Exception("Bruh... how")
        }

    private fun convertToLocation(display: Display?) = if (display != null) Location(textDisplay?.level()?.world, display.x, display.y, display.z, display.yRot, display.xRot) else null


    fun scroll(event: ScrollEvent) {
        val direction = if (event == ScrollEvent.UP) -0.01 else 0.01
        setTeleportDuration(5)

        textDisplay?.let { it.setPos(it.x, it.y + direction, it.z) }
        itemDisplay?.let { it.setPos(it.x, it.y + direction, it.z) }
        blockDisplay?.let { it.setPos(it.x, it.y + direction, it.z) }

        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        playerConnection.send(
            ClientboundMoveEntityPacket.PosRot(
                uniqueId,
                0,
                (direction * 4096).toInt().toShort(),
                0,
                ((yaw * 256.0f) / 360.0f).toInt().toByte(),
                ((pitch * 256.0f) / 360.0f).toInt().toByte(),
                true
            )
        )
    }

    fun updateLocation(location: Location) {
        textDisplay?.let { teleport(player, location) }
        itemDisplay?.let { teleport(player, location) }
        blockDisplay?.let { teleport(player, location) }
    }

    fun remove() {
        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        playerConnection.send(ClientboundRemoveEntitiesPacket(uniqueId))
    }

    fun setTeleportDuration(duration: Int) {
        if (checkVersion(Versions.V1_20_2)) {
            textDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, duration)
            itemDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, duration)
            blockDisplay?.entityData?.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, duration)
        }
    }

    fun renderUpdate() {
        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        val display = textDisplay
            ?: itemDisplay
            ?: blockDisplay
            ?: throw Exception("Bruh... how")

        playerConnection.send(
            ClientboundSetEntityDataPacket(
                display.id,
                display.getEntityData().nonDefaultValues
            )
        )
    }
}
