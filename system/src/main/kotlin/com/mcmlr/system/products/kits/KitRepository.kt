package com.mcmlr.system.products.kits

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.core.add
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.placeholder.placeholders
import com.mcmlr.system.products.data.VaultRepository
import kotlinx.coroutines.flow.flow
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class KitRepository @Inject constructor(
    resources: Resources,
    private val vaultRepository: VaultRepository,
): Repository<KitsStorageModel>(resources.dataFolder()) {

    init {
        loadModel("Kits", "kits", KitsStorageModel())
    }

    val builder: KitModel.Builder = KitModel.Builder()

    fun getCooldown(player: Player, kit: KitModel) = flow {
        generateModelSynced("Kits/Players", player.uniqueId.toString(), PlayerKitStorageModel()) { playerModel ->
            if (kit.kitCooldown < 0 && playerModel.kits.containsKey(kit.uuid)) {
                emit(null)
                return@generateModelSynced
            }

            if (!playerModel.kits.containsKey(kit.uuid)) {
                emit(-1)
                return@generateModelSynced
            }

            val lastUsed = playerModel.kits[kit.uuid] ?: 0
            emit((lastUsed + (kit.kitCooldown * 1000)) - Date().time)
        }
    }

    fun givePlayerKit(player: Player, kit: KitModel, ignorePrice: Boolean = false) = generateModel("Kits/Players", player.uniqueId.toString(), PlayerKitStorageModel()) { playerModel ->
        playerModel.kits[kit.uuid] = Date().time
        playerModel.save {
            kit.items.forEach {
//            @Suppress("DEPRECATION") val key = if (checkVersion("1.21.5-R0.1-SNAPSHOT")) {
//                request.material.keyOrNull
//            } else {
//                request.material.key
//            }

                @Suppress("DEPRECATION") val key = Material.valueOf(it.material).key

                val item = Bukkit.getItemFactory().createItemStack("$key${it.meta}")
                item.amount = it.amount
                player.inventory.add(player.location, item)
            }
            
            val cost = kit.kitPrice / 100.0
            if (!ignorePrice) vaultRepository.economy?.withdrawPlayer(player, cost)

            kit.commands.forEach {
                Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, it.placeholders(player))
            }
        }
    }

    fun addKit(kitModel: KitModel) = save {
        model.kits.add(kitModel)
    }

    fun updateKit(kitModel: KitModel) = save {
        model.kits.replaceAll {
            if (it.uuid == kitModel.uuid) kitModel else it
        }
    }

    fun deleteKit() = save {
        model.kits = model.kits.filter { it.uuid != builder.uuid }.toMutableList()
    }

    fun getKits(): List<KitModel> = model.kits

    fun getKit(uuid: UUID): KitModel? = model.kits.find { it.uuid == uuid }
}

data class PlayerKitStorageModel(val kits: MutableMap<UUID, Long> = mutableMapOf()): ConfigModel()

data class KitsStorageModel(var kits: MutableList<KitModel> = mutableListOf()): ConfigModel()

data class KitModel(
    val uuid: UUID,
    val icon: String,
    val name: String,
    val description: String,
    val kitPrice: Int,
    val kitCooldown: Int,
    val items: List<KitItem>,
    val commands: List<String>,
) {
    class Builder {
        var uuid: UUID? = null
        var icon: String? = null
        var name: String? = null
        var description: String? = null
        var kitPrice: Int? = null
        var kitCooldown: Int? = null
        var items: MutableList<KitItem> = mutableListOf()
        var commands: MutableList<String> = mutableListOf()

        fun checkValid(): String? {
            if (icon == null) return "Please select an icon for your kit!"
            if (name == null) return "Please add a name for your kit!"
            if (description == null) return "Please add a description for your kit!"
            if (kitPrice == null) return "Please add a price for your kit!"
            if (kitCooldown == null) return "Please set a cooldown for your kit!"
            if (items.isEmpty() && commands.isEmpty()) return "You need to add an item or command to your kit!"

            return null
        }

        fun reset() {
            uuid = null
            icon = null
            name = null
            description = null
            kitPrice = null
            kitCooldown = null
            items = mutableListOf()
            commands = mutableListOf()
        }

        fun build(): KitModel? {
            val uuid = uuid
            val icon = icon ?: return null
            val name = name ?: return null
            val description = description ?: return null
            val kitPrice = kitPrice ?: return null
            val kitCooldown = kitCooldown ?: return null
            val items = items.toList()
            val commands = commands.toList()

            reset()

            return KitModel(
                uuid ?: UUID.randomUUID(),
                icon,
                name,
                description,
                kitPrice,
                kitCooldown,
                items,
                commands,
            )
        }

        fun icon(icon: Material): Builder {
            this.icon = icon.name
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun description(description: String): Builder {
            this.description = description
            return this
        }

        fun price(price: Int): Builder {
            this.kitPrice = price
            return this
        }

        fun cooldown(cooldown: Int): Builder {
            this.kitCooldown = cooldown
            return this
        }

        fun items(items: MutableList<KitItem>): Builder {
            this.items = items
            return this
        }

        fun commands(commands: MutableList<String>): Builder {
            this.commands = commands
            return this
        }
    }
}

data class KitItem(val material: String, val amount: Int, val meta: String?)
