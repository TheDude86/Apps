package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.minetunes.TrackInteractor
import com.mcmlr.system.products.minetunes.nbs.NBSDecoder
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import javax.inject.Inject

@AppScope
class MusicRepository @Inject constructor(
    private val resources: Resources,
    private val service: DownloadService,
) {

    fun downloadTrack(track: Track): Flow<Song?> {
        val songFlow = MutableSharedFlow<Song?>()
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.download("https://firebasestorage.googleapis.com/v0/b/mc-apps-9477a.firebasestorage.app/o/apps%2Fminetunes%2F${track.downloadUrl}")
            var input: InputStream? = null
            try {
                input = response.body()?.byteStream() ?: return@launch

                val fileName = "${track.artist.replace("/", "%!")} - ${track.song}.nbs"
                val fos = File(resources.dataFolder(), "${File.separator}Mine Tunes${File.separator}Songs${File.separator}$fileName")
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
        }

        return songFlow
    }

    fun loadSong(song: String): Song? {
        val homes = File(resources.dataFolder(), "Mine Tunes/Songs/$song")
        return NBSDecoder.parse(homes.inputStream())
    }

    fun songList(): List<String> {
        val songDirectory = File(resources.dataFolder(), "Mine Tunes/Songs")
        return songDirectory.list().toList()
    }

}
