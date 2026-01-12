package com.mcmlr.system.products.minetunes.player

import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.emitBackground
import com.mcmlr.system.products.minetunes.MusicRepository
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.util.NoteUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.Bukkit
import java.util.*

class MusicPlayer(
    private val playerId: UUID,
    private val musicRepository: MusicRepository,
) {
    companion object {
        const val SONG_COMPLETED = "complete"
        const val SONG_PAUSED = "pause"
        const val PLAYER_LEFT = "left"
    }

    private var activeJob: Job? = null
    private var songProgressStream = MutableStateFlow<Short>(0)
    private var eventStream = MutableSharedFlow<MusicPlayerAction>()
    private var songList: List<Track> = listOf()
    var playlist = Playlist()
    var activeSong: Song? = null
    var isShuffled = false
    var isLooped = true
    var tick: Short = 0
    var songIndex = 0

    //TODO: Come up with better way to track if is playing, jobs cancel async and can lead to race conditions
    fun isPlaying(): Boolean {
        return activeJob?.isActive == true
    }

    fun isPlaylistLoaded(): Boolean = songList.isNotEmpty()

    fun getSongProgressStream(): Flow<Short> = songProgressStream

    fun getActionStream(): Flow<MusicPlayerAction> = eventStream

    fun loop() {
        isLooped = !isLooped
    }

    fun shuffle() {
        isShuffled = !isShuffled

        songList = if (isShuffled) {
            songList.shuffled()
        } else {
            playlist.songs
        }
    }

    fun startPlaylist(): Flow<Short> {
        stopSong()
        eventStream.emitBackground(MusicPlayerAction.PLAY)
        songIndex = 0
        return play()
    }

    fun startSong(index: Int = 0): Flow<Short> {
        stopSong()
        eventStream.emitBackground(MusicPlayerAction.PLAY)
        songIndex = index
        return play()
    }

    fun setDefaultPlaylist(playlist: Playlist) {
        if (this.playlist.songs.isNotEmpty()) return

        this.playlist = playlist
        songList = playlist.songs
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
        eventStream.emitBackground(MusicPlayerAction.NEXT)
        songIndex = (songIndex + 1) % songList.size
        play()
    }

    fun goToLastSong() {
        stopSong()
        eventStream.emitBackground(MusicPlayerAction.LAST)
        songIndex = (songIndex - 1) % songList.size
        play()
    }



    fun play(): Flow<Short> {
        if (activeSong == null) {
            val currentTrack = songList[songIndex]

            musicRepository.downloadTrack(currentTrack)
                .collectFirst(DudeDispatcher()) {
                    val song = it ?: return@collectFirst
                    updateSong(song)
                    setNextSongListener()
                }
        } else {
            resumeSong()
            setNextSongListener()
        }

        return songProgressStream
    }

    fun pause() {
        eventStream.emitBackground(MusicPlayerAction.STOP)
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




    fun playSong(): Flow<Short> {
        eventStream.emitBackground(MusicPlayerAction.PLAY)
        return play()
    }

    fun updateSong(song: Song) {
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
                        val player = Bukkit.getPlayer(playerId)

                        if (player != null) {
                            player.playSound(player.eyeLocation, NoteUtils.getInstrumentName(note.instrument), volume.toFloat(), pitch)
                        } else {
                            activeJob?.cancel(PLAYER_LEFT)
                        }

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

enum class MusicPlayerAction {
    PLAY,
    STOP,
    NEXT,
    LAST,
}
