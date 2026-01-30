import Versions
import com.mojang.math.Transformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f

class PaperFactory: NMSFactory {

    private val factory: NMSFactory = when(Versions.getByName(Bukkit.getBukkitVersion()).versionNumber) {
        17 -> Factory_1_21_11()
        else -> Factory_1_21_8()
    }

    override fun serverLevel(player: Player): ServerLevel = factory.serverLevel(player)

    override fun serverLevel(location: Location): ServerLevel = factory.serverLevel(location)

    override fun serverPlayer(player: Player): ServerPlayer = factory.serverPlayer(player)

    override fun itemStack(itemStack: org.bukkit.inventory.ItemStack): ItemStack = factory.itemStack(itemStack)

    override fun transformation(
        offset: Vector3f,
        leftRotation: Quaternionf,
        scale: Vector3f,
        rightRotation: Quaternionf
    ): Transformation = factory.transformation(offset, leftRotation, scale, rightRotation)
}

//TODO: Move to new module
enum class Versions(val versionName: String, val versionNumber: Int) {
    UNKNOWN("", -1),
    V1_19_4("1.19.4-R0.1-SNAPSHOT", 0),
    V1_20("1.20-R0.1-SNAPSHOT", 1),
    V1_20_1("1.20.1-R0.1-SNAPSHOT", 2),
    V1_20_2("1.20.2-R0.1-SNAPSHOT", 3),
    V1_20_4("1.20.4-R0.1-SNAPSHOT", 4),
    V1_20_5("1.20.5-R0.1-SNAPSHOT", 5),
    V1_20_6("1.20.6-R0.1-SNAPSHOT", 6),
    V1_21("1.21-R0.1-SNAPSHOT", 7),
    V1_21_1("1.21.1-R0.1-SNAPSHOT", 8),
    V1_21_3("1.21.3-R0.1-SNAPSHOT", 9),
    V1_21_4("1.21.4-R0.1-SNAPSHOT", 10),
    V1_21_5("1.21.5-R0.1-SNAPSHOT", 11),
    V1_21_6("1.21.6-R0.1-SNAPSHOT", 12),
    V1_21_7("1.21.7-R0.1-SNAPSHOT", 13),
    V1_21_8("1.21.8-R0.1-SNAPSHOT", 14),
    V1_21_9("1.21.9-R0.1-SNAPSHOT", 15),
    V1_21_10("1.21.10-R0.1-SNAPSHOT", 16),
    V1_21_11("1.21.11-R0.1-SNAPSHOT", 17),
    V1_21_12("1.21.11-R0.1-SNAPSHOT", 17),;

    companion object {
        fun getByName(name: String): Versions = entries.find { it.versionName == name } ?: UNKNOWN

        fun getByVersionNumber(versionNumber: Int): Versions = entries.find { it.versionNumber == versionNumber } ?: UNKNOWN
    }
}
