package com.mcmlr.blocks.api.packets

import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.packetFactory
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

class ItemDisplayPacket(
    private val players: List<Player>,
    private var location: Location,
    private var item: ItemStack,
    private var teleportDuration: Int = 3,
    private var scale: Vector3f = Vector3f(1f, 1f, 1f),
): DisplayPacket<Display.ItemDisplay>(players, location) {
    override val display = Display.ItemDisplay(EntityType.ITEM_DISPLAY, packetFactory.serverLevel(location))

    init {
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        display.addTag("mcmlr.apps")
        display.itemStack = packetFactory.itemStack(item)

        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, teleportDuration)
        display.setTransformation(packetFactory.transformation(
            Vector3f(0f, 0f, 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            scale,
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        render()
    }

    fun update(
        location: Location? = null,
        item: ItemStack? = null,
        background: Color? = null ,
        opacity: Int? = null,
        teleportDuration: Int? = null,
    ) {
        item?.let { this.item = it }
        teleportDuration?.let { this.teleportDuration = it }

        display.itemStack = packetFactory.itemStack(this.item)
        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, this.teleportDuration)

        if (location != null) teleport(location)

        if (item != null ||
            location != null ||
            background != null ||
            opacity != null ||
            teleportDuration != null
        ) {
            renderUpdate()
        }
    }
}