package com.mcmlr.system.products.teleport

import com.mcmlr.system.dagger.EnvironmentScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class TeleportRepository @Inject constructor() {
    companion object {
        private const val TWO_MINUTES_IN_MILLISECONDS = 1000 * 60 * 2
    }

    private val requestsMap = HashMap<UUID, MutableList<TeleportRequestModel>>()

    fun getOnlinePlayers(playerId: UUID): List<Player> = Bukkit.getOnlinePlayers().filter { it.uniqueId != playerId }.toList()

    fun sendRequest(sender: Player, receiver: Player, type: TeleportRequestType): TeleportStatus {
        val model = TeleportRequestModel(sender, type, Date().time + TWO_MINUTES_IN_MILLISECONDS)
        val requestList = requestsMap[receiver.uniqueId]
        if (requestList == null) {
            requestsMap[receiver.uniqueId] = mutableListOf(model)
            return TeleportStatus.NEW
        } else {
            val existingRequest = requestList.find { it.sender.uniqueId == sender.uniqueId }
            if (existingRequest == null) {
                requestList.add(model)
                return TeleportStatus.NEW
            } else if (existingRequest.type != type) {
                requestList.remove(existingRequest)
                requestList.add(model)
                return TeleportStatus.UPDATE
            }
        }
        return TeleportStatus.FAILED
    }

    fun getRequests(playerId: UUID): List<TeleportRequestModel> {
        val requests = requestsMap[playerId] ?: listOf()
        val validRequests = requests.filter { it.timeout > Date().time }.toMutableList()
        requestsMap[playerId] = validRequests

        return validRequests
    }

    fun deleteRequest(playerId: UUID, request: TeleportRequestModel) {
        requestsMap[playerId]?.remove(request)
    }
}

data class TeleportRequestModel(
    val sender: Player,
    val type: TeleportRequestType,
    val timeout: Long
)

enum class TeleportStatus {
    NEW,
    UPDATE,
    FAILED,
}

enum class TeleportRequestType {
    GOTO,
    COME,
}
