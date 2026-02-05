import com.mojang.math.Transformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

interface NMSFactory {

    fun serverLevel(player: Player): ServerLevel

    fun serverLevel(location: Location): ServerLevel

    fun serverPlayer(player: Player): ServerPlayer

    fun itemStack(itemStack: ItemStack): net.minecraft.world.item.ItemStack

    fun transformation(
        offset: Vector3f,
        leftRotation: Quaternionf,
        scale: Vector3f,
        rightRotation: Quaternionf,
    ): Transformation
}