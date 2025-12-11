package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.minetunes.nbs.NBSDecoder
import com.mcmlr.system.products.minetunes.nbs.data.Song
import java.io.File
import javax.inject.Inject

@AppScope
class MusicRepository @Inject constructor(
    private val resources: Resources,
) {

    fun loadSong(song: String): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes/Songs/$song")
        return NBSDecoder.parse(homes.inputStream())
    }

    fun loadSong(): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes/Songs/bib.nbs")
        return NBSDecoder.parse(homes.inputStream())
    }

    fun songList(): List<String> {
        val songDirectory = File(resources.dataFolder(), "Mine Tunes/Songs")
        return songDirectory.list().toList()
    }

}
