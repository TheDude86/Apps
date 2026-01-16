package com.mcmlr.system.products.minetunes

import com.google.gson.Gson
import com.mcmlr.blocks.api.Resources
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.minetunes.nbs.NBSDecoder
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@EnvironmentScope
class MusicRepository @Inject constructor(
    private val resources: Resources,
    private val musicCache: MusicCache,
) {
    companion object {
        private const val TRACK_PLAYS_DELAY = 1
    }

    private val songMap = mutableMapOf<String, Int>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (songMap.isNotEmpty()) {
                    val url = "https://searchartist-l5cijtvgrq-uc.a.run.app"
                    val data = Gson().toJson(songMap)

                    val request = Request.Builder().url("$url/?data=$data").build()
                    OkHttpClient().newCall(request).execute() //TODO: Move OkHTTPClient to dependency
                }

                delay(TRACK_PLAYS_DELAY.hours)
            }
        }
    }

    fun downloadTrack(track: Track): Flow<Song?> = musicCache.lookup(track)

    fun trackSongPlayed(downloadUrl: String) {
        songMap[downloadUrl] = (songMap[downloadUrl] ?: 0) + 1
    }
}
