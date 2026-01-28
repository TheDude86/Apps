import com.mojang.math.Transformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f

class PaperFactory: NMSFactory {
    override fun serverLevel(player: Player): ServerLevel = (player.world as CraftWorld).handle

    override fun serverLevel(location: Location): ServerLevel = (location.world as CraftWorld).handle

    override fun serverPlayer(player: Player): ServerPlayer = (player as CraftPlayer).handle

    override fun itemStack(itemStack: org.bukkit.inventory.ItemStack): ItemStack = CraftItemStack.asNMSCopy(itemStack)

    override fun transformation(
        offset: Vector3f,
        leftRotation: Quaternionf,
        scale: Vector3f,
        rightRotation: Quaternionf
    ): Transformation = Transformation(offset, leftRotation, scale, rightRotation)
}