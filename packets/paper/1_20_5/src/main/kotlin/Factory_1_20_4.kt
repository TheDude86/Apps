import com.mojang.math.Transformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

class Factory_1_20_4: NMSFactory {
    override fun serverLevel(player: Player): ServerLevel = (player.world as CraftWorld).handle

    override fun serverLevel(location: Location): ServerLevel = (location.world as CraftWorld).handle

    override fun serverPlayer(player: Player): ServerPlayer = (player as CraftPlayer).handle

    override fun itemStack(itemStack: ItemStack): net.minecraft.world.item.ItemStack = CraftItemStack.asNMSCopy(itemStack)

    override fun transformation(
        offset: Vector3f,
        leftRotation: Quaternionf,
        scale: Vector3f,
        rightRotation: Quaternionf
    ): Transformation = Transformation(offset, leftRotation, scale, rightRotation)
}