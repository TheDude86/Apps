package com.mcmlr.blocks.api.packets

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.DudeDisplay.Companion.DELTA_MINIMUM
import com.mcmlr.packetFactory
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class DisplayPacket<T: Display>(
    private val players: List<Player>,
    private var location: Location,
) {

    protected abstract val display: T

    protected fun render() {
        players.forEach { player ->
            val handle = packetFactory.serverPlayer(player)
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
    }

    protected fun renderUpdate() {
        players.forEach { player ->
            val handle = packetFactory.serverPlayer(player)
            val playerConnection = handle.connection

            playerConnection.send(
                ClientboundSetEntityDataPacket(
                    display.id,
                    display.getEntityData().nonDefaultValues
                )
            )
        }
    }

    protected fun teleport(location: Location) {
        val l = location.clone()
        l.x = max(this.location.x - 7.0, min(this.location.x + 7.0, l.x))
        l.y = max(this.location.y - 7.0, min(this.location.y + 7.0, l.y))
        l.z = max(this.location.z - 7.0, min(this.location.z + 7.0, l.z))

        val dx = l.x - this.location.x
        val dy = l.y - this.location.y
        val dz = l.z - this.location.z
        val nx = if (abs(dx) >= DELTA_MINIMUM) l.x else this.location.x
        val ny = if (abs(dy) >= DELTA_MINIMUM) l.y else this.location.y
        val nz = if (abs(dz) >= DELTA_MINIMUM) l.z else this.location.z
        val px = if (abs(dx) >= DELTA_MINIMUM) dx else 0.0
        val py = if (abs(dy) >= DELTA_MINIMUM) dy else 0.0
        val pz = if (abs(dz) >= DELTA_MINIMUM) dz else 0.0

        display.setPos(nx, ny, nz)
        display.yRot = l.yaw
        display.xRot = l.pitch
        this.location = l

        players.forEach { player ->
            val handle = packetFactory.serverPlayer(player)
            val playerConnection = handle.connection
            playerConnection.send(
                ClientboundMoveEntityPacket.PosRot(
                    display.id,
                    (px * 4096).toInt().toShort(),
                    (py * 4096).toInt().toShort(),
                    (pz * 4096).toInt().toShort(),
                    ((l.yaw * 256.0f) / 360.0f).toInt().toByte(),
                    ((l.pitch * 256.0f) / 360.0f).toInt().toByte(),
                    true
                )
            )
        }


    }

    fun remove() {
        players.forEach { player ->
            val handle = packetFactory.serverPlayer(player)
            val playerConnection = handle.connection
            playerConnection.send(ClientboundRemoveEntitiesPacket(display.id))
        }
    }
}