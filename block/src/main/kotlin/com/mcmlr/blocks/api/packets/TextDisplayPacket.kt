package com.mcmlr.blocks.api.packets

import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.checkVersion
import com.mcmlr.packetFactory
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f

class TextDisplayPacket(
    private val players: List<Player>,
    private var location: Location,
    private var text: String = ".....",
    private var background: Color = Color.fromARGB(0x40000001),
    private var opacity: Int = 100,
    private var teleportDuration: Int = 3,
): DisplayPacket<Display.TextDisplay>(players, location) {
    override val display = Display.TextDisplay(EntityType.TEXT_DISPLAY, packetFactory.serverLevel(location))

    init {
        display.setPos(location.x, location.y, location.z)
        display.yRot = location.yaw
        display.xRot = location.pitch

        display.addTag("mcmlr.apps")
        display.text = Component.literal(text)
        display.textOpacity = opacity.toByte()
        display.entityData.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, background.asARGB())

        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, teleportDuration)
        display.setTransformation(packetFactory.transformation(
            Vector3f(0f, 0f, 0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(4f, 4f, 1f),
            Quaternionf(0f, 0f, 0f, 1f)
        ))

        render()
    }

    fun update(
        location: Location? = null,
        text: String? = null ,
        background: Color? = null ,
        opacity: Int? = null,
        teleportDuration: Int? = null,
    ) {
        text?.let { this.text = it }
        background?.let { this.background = it }
        opacity?.let { this.opacity = it }
        teleportDuration?.let { this.teleportDuration = it }

        display.text = Component.literal(this.text)
        display.textOpacity = this.opacity.toByte()
        display.entityData.set<Int>(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, this.background.asARGB())
        if (checkVersion(Versions.V1_20_2)) display.entityData.set<Int>(Display.TextDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, this.teleportDuration)

        if (location != null) teleport(location)

        if (text != null ||
            location != null ||
            background != null ||
            opacity != null ||
            teleportDuration != null
        ) {
            renderUpdate()
        }
    }
}