package com.mcmlr.system.products.minetunes.player

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.util.NoteUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.Date

class MusicPlayer() {

    private var activeSong: Job? = null

    fun playSong(song: Song, player: Player) {
        activeSong?.cancel()
        activeSong = CoroutineScope(Dispatchers.IO).launch {
            var tick = 0

            while (true) {
                val start = Date().time

                CoroutineScope(DudeDispatcher()).launch {
                    song.layersMap.values.forEach {
                        val note = it.notesMap[tick] ?: return@forEach
                        val volume = it.volume
                        val pitch = NoteUtils.pitch(note.key, note.pitch)
                        player.playSound(player.eyeLocation, NoteUtils.getInstrumentName(note.instrument), volume.toFloat(), pitch)
                    }
                }

                tick++
                val delay = song.delay * 50
                val wait = delay - (Date().time - start)
                if (wait > 0) delay(wait.toLong())
            }
        }
    }

}
