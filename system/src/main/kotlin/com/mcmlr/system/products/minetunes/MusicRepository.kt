package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.minetunes.nbs.NBSDecoder
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

@EnvironmentScope
class MusicRepository @Inject constructor(
    private val resources: Resources,
    private val musicCache: MusicCache,
) {



    fun downloadTrack(track: Track): Flow<Song?> = musicCache.lookup(track)

    fun loadSong(song: String): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes${File.separator}Cache${File.separator}$song")
        return NBSDecoder.parse(homes.inputStream())
    }

    fun songList(): List<String> {
        val songDirectory = File(resources.dataFolder(), "Mine Tunes${File.separator}Cache")
        return songDirectory.list().toList()
    }

}
