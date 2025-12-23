package com.mcmlr.system.products.minetunes.player

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.minetunes.MusicRepository
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.util.NoteUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.Date
import javax.inject.Inject

@AppScope
class MusicPlayer @Inject constructor(
    private val player: Player,
    private val musicRepository: MusicRepository,
) {
    companion object {
        const val SONG_COMPLETED = "complete"
        const val SONG_PAUSED = "pause"
    }

    private var activeJob: Job? = null
    private var activeSong: Song? = null
    private var songProgressStream = MutableStateFlow<Short>(0)
    private var playlist = Playlist()
    var tick: Short = 0
    var songIndex = 0

    fun updatePlaylist(playlist: Playlist) {
        stopSong()
        this.playlist = playlist
    }

    fun play(): Flow<Short> {
        if (activeSong == null) {
            songProgressStream = MutableStateFlow(0)
            val currentTrack = playlist.songs[songIndex]

            musicRepository.downloadTrack(currentTrack)
                .collectFirst(DudeDispatcher()) {
                    val song = it ?: return@collectFirst
                    playSong(song, player)
                    setNextSongListener()
                }
        } else {
            resumeSong()
            setNextSongListener()
        }

        return songProgressStream
    }

    fun pause() {
        activeJob?.cancel(SONG_PAUSED)
    }

    private fun setNextSongListener() {
        activeJob?.invokeOnCompletion {
            if (it?.message == SONG_COMPLETED) {
                tick = 0
                songIndex++
                activeSong = null
                if (songIndex == playlist.songs.size) songIndex = 0

                play()
            }
        }
    }






    fun playSong(song: Song, player: Player): Flow<Short> {
        songProgressStream = MutableStateFlow(0)
        activeJob?.cancel()
        activeSong = song
        resumeSong()

        return songProgressStream
    }

    fun pauseSong() {
        activeJob?.cancel(SONG_PAUSED)
    }

    fun stopSong() {
        activeJob?.cancel()
        activeSong = null
        tick = 0
    }

    private fun resumeSong() {
        activeJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val start = Date().time
                val song = activeSong ?: return@launch

                CoroutineScope(DudeDispatcher()).launch {
                    song.layersMap.values.forEach {
                        val note = it.notesMap[tick.toInt()] ?: return@forEach
                        val volume = it.volume
                        val pitch = NoteUtils.pitch(note.key, note.pitch)
                        player.playSound(player.eyeLocation, NoteUtils.getInstrumentName(note.instrument), volume.toFloat(), pitch)
                    }
                }

                songProgressStream.emit(tick)

                if (tick >= song.length) cancel(SONG_COMPLETED)

                tick++
                val delay = song.delay * 50
                val wait = delay - (Date().time - start)
                if (wait > 0) delay(wait.toLong())
            }
        }
    }

}
