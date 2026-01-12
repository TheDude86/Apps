package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.minetunes.nbs.NBSDecoder
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.Date
import javax.inject.Inject

@EnvironmentScope
class MusicCache @Inject constructor(
    private val resources: Resources,
    private val service: DownloadService,
): Repository<MusicCacheModel>(resources.dataFolder()) {

    init {
        loadModel("Mine Tunes${File.separator}Cache", "tracks", MusicCacheModel())
    }

    fun lookup(track: Track): Flow<Song?> {
        val songFlow = MutableSharedFlow<Song?>()

        CoroutineScope(Dispatchers.IO).launch {
            val fileName = "${track.artist.replace("/", "%!")} - ${track.song}.nbs"
            if (model.entries.containsKey(track.downloadUrl)) {
                val song = loadSong(fileName)
                songFlow.emit(song)
            } else {
                val response = service.download("https://firebasestorage.googleapis.com/v0/b/mc-apps-9477a.firebasestorage.app/o/apps%2Fminetunes%2F${track.downloadUrl}")
                var input: InputStream? = null
                try {
                    input = response.body()?.byteStream() ?: return@launch

                    val fileName = "${track.artist.replace("/", "%!")} - ${track.song}.nbs"
                    val fos = File(resources.dataFolder(), "${File.separator}Mine Tunes${File.separator}Cache${File.separator}$fileName")
                    fos.outputStream().use { output ->
                        val buffer = ByteArray(4 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }

                    val song = loadSong(fileName)
                    songFlow.emit(song)
                } catch (e: Throwable) {
                    println("Error: $e")
                } finally {
                    input?.close()
                }

                save {
                    model.entries[track.downloadUrl] = TrackCacheModel(track, Date().time)
                }
            }
        }

        return songFlow
    }

    private fun loadSong(song: String): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes${File.separator}Cache${File.separator}$song")
        return NBSDecoder.parse(homes.inputStream())
    }
}

data class MusicCacheModel(
    val entries: MutableMap<String, TrackCacheModel> = mutableMapOf()
): ConfigModel()

data class TrackCacheModel(
    val track: Track,
    var lastPlayed: Long,
)
