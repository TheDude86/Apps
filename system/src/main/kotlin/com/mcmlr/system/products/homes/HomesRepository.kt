package com.mcmlr.system.products.homes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.AppConfigModel
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.AppScope
import com.mcmlr.system.EnvironmentScope
import com.mcmlr.system.products.data.CooldownRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class HomeListRepository @Inject constructor(
    private val resources: Resources,
): Repository<PlayerHomesModel>(resources.dataFolder()) {

    fun getHomes(player: Player): PlayerHomesModel? {
        val homes = File(resources.dataFolder(), "Homes/Players/${player.uniqueId}.json")
        if (!homes.exists()) {
            return null
        }

        val homeInputStream: InputStream = homes.inputStream()
        val homeInputString = homeInputStream.bufferedReader().use { it.readText() }

        return Gson().fromJson(homeInputString, PlayerHomesModel::class.java)
    }
}

@AppScope
class HomesRepository @Inject constructor(
    private val resources: Resources,
    private val cooldownRepository: CooldownRepository,
    private val homesConfigRepository: HomesConfigRepository,
) {
    private val playerHomesMap = HashMap<UUID, PlayerHomesModel>()
    private var updatingHome: HomeModel? = null

    fun teleport(player: Player, location: Location) {
        player.teleport(location)
        cooldownRepository.addPlayerLastHomeTime(player)
    }

    fun canTeleport(player: Player): Long {
        val lastTeleport = cooldownRepository.getPlayerLastHomeTime(player)
        return (lastTeleport + (homesConfigRepository.model.cooldown * 1000)) - Date().time
    }

    fun latest(player: Player): PlayerHomesModel? {
        if (!playerHomesMap.containsKey(player.uniqueId)) {
            playerHomesMap[player.uniqueId] = PlayerHomesModel(listOf())
        }
        return playerHomesMap[player.uniqueId]
    }

    fun getHomes(player: Player) = flow {
        val homes = File(resources.dataFolder(), "Homes/Players/${player.uniqueId}.json")
        if (!homes.exists()) {
            emit(PlayerHomesModel(listOf()))
            return@flow
        }

        val homeInputStream: InputStream = homes.inputStream()
        val homeInputString = homeInputStream.bufferedReader().use { it.readText() }

        val playerHomes = Gson().fromJson(homeInputString, PlayerHomesModel::class.java)
        cachePlayerHomes(player.uniqueId, playerHomes)
        emit(playerHomes)
    }

    private fun cachePlayerHomes(playerId: UUID, playerHomesModel: PlayerHomesModel) {
        CoroutineScope(DudeDispatcher()).launch {
            playerHomesMap[playerId] = playerHomesModel
        }
    }

    fun updateHome(homeModel: HomeModel?) {
        updatingHome = homeModel
    }

    fun getUpdateBuilder(): HomeModel.Builder? {
        val update = updatingHome ?: return null
        return HomeModel.Builder()
            .name(update.name)
            .icon(update.icon)
            .uuid(update.uuid)
            .location(
                Location(
                    Bukkit.getWorld(update.world),
                    update.x,
                    update.y,
                    update.z,
                    update.yaw,
                    update.pitch,
                )
            )
    }

    fun saveHome(player: Player, homeModel: HomeModel) {
        val homes = File(resources.dataFolder(), "Homes/Players")

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val existingHomes = playerHomesMap[player.uniqueId]?.homes?.filter { it.uuid != homeModel.uuid }
        val homesList = mutableListOf(homeModel)
        homesList.addAll(existingHomes ?: listOf())
        val newPlayerHomesModel = PlayerHomesModel(homesList)
        cachePlayerHomes(player.uniqueId, newPlayerHomesModel)

        val homeConfigString = gson.toJson(newPlayerHomesModel)
        val homeWriter = FileWriter(File(homes.path, "${player.uniqueId}.json"))
        homeWriter.append(homeConfigString)
        homeWriter.close()
    }

    fun deleteHome(player: Player, homeModel: HomeModel) {
        val homes = File(resources.dataFolder(), "Homes/Players")

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val updatedHomes = playerHomesMap[player.uniqueId]?.homes?.filter { it.uuid != homeModel.uuid } ?: return
        val newPlayerHomesModel = PlayerHomesModel(updatedHomes)
        cachePlayerHomes(player.uniqueId, newPlayerHomesModel)

        val homeConfigString = gson.toJson(newPlayerHomesModel)
        val homeWriter = FileWriter(File(homes.path, "${player.uniqueId}.json"))
        homeWriter.append(homeConfigString)
        homeWriter.close()
    }

}

data class PlayerHomesModel(
    val homes: List<HomeModel>,
): ConfigModel()

data class HomeModel(
    val uuid: UUID,
    val icon: Material,
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
) {


    class Builder {
        var icon: Material? = null
        var name: String? = null
        var location: Location? = null
        var uuid: UUID? = null

        fun icon(icon: Material?): Builder {
            this.icon = icon
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun location(location: Location): Builder {
            this.location = location
            return this
        }

        fun uuid(uuid: UUID): Builder {
            this.uuid = uuid
            return this
        }

        fun build(): HomeModel? {
            return icon?.let { icon ->
                name?.let { name ->
                    location?.let { location ->
                        HomeModel(
                            uuid = uuid ?: UUID.randomUUID(),
                            icon = icon,
                            name = name,
                            x = location.x,
                            y = location.y,
                            z = location.z,
                            yaw = 0f,
                            pitch = 0f,
                            world = location.world?.name ?: "null",
                        )
                    }
                }
            }
        }
    }
}
