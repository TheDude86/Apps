package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.minetunes.blocks.OrderPlaylistAction
import com.mcmlr.system.products.minetunes.player.Icon
import com.mcmlr.system.products.minetunes.player.IconType
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import org.bukkit.entity.Player
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@AppScope
class LibraryRepository @Inject constructor(
    player: Player,
    private val resources: Resources,
): Repository<LibraryModel>(resources.dataFolder()) {
    companion object {
        const val FAVORITES_UUID = "favorite"
    }

    init {
        loadModel(
            "Mine Tunes${File.separator}Players",
            player.uniqueId.toString(),
            LibraryModel(
                mutableListOf(
                    Playlist(
                        FAVORITES_UUID,
                        Icon(IconType.MATERIAL, "REDSTONE"),
                        "Favorites",
                        true,
                    )
                )
            )
        )
    }

    fun updatePlaylistSongOrder(playlistId: String, songIndex: Int, action: OrderPlaylistAction) = save {
        val playlist = model.playlists.find { it.uuid == playlistId } ?: return@save
        val offset = if (action == OrderPlaylistAction.UP) -1 else 1
        if (songIndex + offset !in 0..<playlist.songs.size) return@save

        val swap = playlist.songs[songIndex]
        playlist.songs[songIndex] = playlist.songs[songIndex + offset]
        playlist.songs[songIndex + offset] = swap
    }

    fun getPlaylists() = model.playlists

    fun isFavorite(track: Track): Boolean = model.playlists.find { it.uuid == FAVORITES_UUID }?.songs?.find { it.downloadUrl == track.downloadUrl } != null

    fun addToFavorites(track: Track) = save {
        model.playlists.find { it.uuid == FAVORITES_UUID }?.songs?.add(track)
    }

    fun deletePlaylist(playlistId: String) = save {
        model.playlists.removeIf { it.uuid == playlistId }
    }

    fun addToPlaylist(track: Track, playlistId: String): Boolean {
        val playlist = model.playlists.find { it.uuid == playlistId } ?: return false
        if (playlist.songs.find { it.downloadUrl == track.downloadUrl } != null) return false
        save {
            playlist.songs.add(track)
        }

        return true
    }

    fun removeFromPlaylist(track: Track, playlistId: String): Boolean {
        val playlist = model.playlists.find { it.uuid == playlistId } ?: return false
        if (playlist.songs.find { it.downloadUrl == track.downloadUrl } == null) return false
        save {
            playlist.songs.removeIf { it.downloadUrl == track.downloadUrl }
        }

        return true
    }

    fun createPlaylist(iconData: String?, name: String?) = save {
        val icon = if (iconData != null) Icon(IconType.MATERIAL, iconData) else null
        val time = Date().time
        model.playlists.add(Playlist(uuid = UUID.randomUUID().toString(), icon = icon, name = name, createdDate = time, lastUsedDate = time))
    }

    fun updatePlaylist(iconData: String?, name: String?, playlistId: String) = save {
        val icon = if (iconData != null) Icon(IconType.MATERIAL, iconData) else null
        val playlist = model.playlists.find { it.uuid == playlistId } ?: return@save
        playlist.name = name
        playlist.icon = icon
    }

    fun updateLastPlayed(updatedPlaylist: Playlist) = save {
        val playlistId = updatedPlaylist.uuid ?: return@save
        val playlist = model.playlists.find { it.uuid == playlistId } ?: return@save
        playlist.lastUsedDate = Date().time
    }

    fun getModel() = model
}

data class LibraryModel(
    val playlists: MutableList<Playlist> = mutableListOf()
): ConfigModel()
