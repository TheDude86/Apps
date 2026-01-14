package com.mcmlr.system.products.minetunes

import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.minetunes.player.MusicPlayer
import org.bukkit.entity.Player
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class MusicPlayerRepository @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    private val musicPlayerMap = mutableMapOf<UUID, MusicPlayer>()

    fun getMusicPlayer(player: Player): MusicPlayer {
        musicPlayerMap[player.uniqueId]?.let {
            return it
        }

        val newPlayer = MusicPlayer(player.uniqueId, musicRepository)
        musicPlayerMap[player.uniqueId] = newPlayer
        return newPlayer
    }

    fun removeMusicPlayer(player: Player) {
        musicPlayerMap[player.uniqueId]?.pause()
        musicPlayerMap.remove(player.uniqueId)
    }
}