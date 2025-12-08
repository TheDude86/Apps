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

    fun loadSong(): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes/Songs/africa.nbs")
        return NBSDecoder.parse(homes.inputStream())
    }

}
