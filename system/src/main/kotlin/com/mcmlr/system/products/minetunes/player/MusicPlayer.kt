package com.mcmlr.system.products.minetunes.player

import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.system.products.minetunes.MusicRepository
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.util.NoteUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.entity.Player
import java.util.*

class MusicPlayer(
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
    private var songList: List<Track> = listOf()
    var isShuffled = false
    var isLooped = true
    var tick: Short = 0
    var songIndex = 0

    fun getActiveSongStream(): Flow<Short>? = if (activeJob == null) null else songProgressStream

    fun loop() {
        isLooped = !isLooped
    }

    fun shuffle() {
        isShuffled = !isShuffled

        if (isShuffled) {
            songList = songList.shuffled()
        } else {
            songList = playlist.songs
        }
    }

    fun startPlaylist(): Flow<Short> {
        stopSong()
        songIndex = 0
        return play()
    }

    fun startSong(index: Int = 0): Flow<Short> {
        stopSong()
        songIndex = index
        return play()
    }

    fun updatePlaylist(playlist: Playlist) {
        this.playlist = playlist
        songList = playlist.songs
        isShuffled = false
        isLooped = true
        tick = 0
        songIndex = 0
    }

    fun getCurrentSong(): Song? = activeSong

    fun getCurrentTrack(): Track = songList[songIndex]

    fun goToNextSong() {
        stopSong()
        songIndex = (songIndex + 1) % songList.size
        play()
    }

    fun goToLastSong() {
        stopSong()
        songIndex = (songIndex + 1) % songList.size
        play()
    }

    fun play(): Flow<Short> {
        stopSong()
        if (activeSong == null) {
            val currentTrack = songList[songIndex]

            musicRepository.downloadTrack(currentTrack)
                .collectFirst(DudeDispatcher()) {
                    val song = it ?: return@collectFirst
                    playSong(song)
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
                if (songIndex == songList.size) {
                    songIndex = 0
                    if (!isLooped) return@invokeOnCompletion
                }

                play()
            }
        }
    }






    fun playSong(song: Song) {
        activeJob?.cancel()
        activeSong = song
        resumeSong()
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
