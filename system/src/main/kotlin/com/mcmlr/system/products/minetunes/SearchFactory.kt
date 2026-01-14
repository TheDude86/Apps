package com.mcmlr.system.products.minetunes

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

object SearchFactory {
    fun search(searchTerm: String, searchState: SearchState): Flow<List<Track>> {
        val flow = MutableSharedFlow<List<Track>>()

        CoroutineScope(Dispatchers.IO).launch {
            val url = if (searchState == SearchState.SONG) "https://searchsong-l5cijtvgrq-uc.a.run.app" else "https://searchartist-l5cijtvgrq-uc.a.run.app"

            val request = Request.Builder().url("$url/?search=$searchTerm").build()
            val response = OkHttpClient().newCall(request).execute() //TODO: Move OkHTTPClient to dependency
            val body = response.body?.string() ?: return@launch
            val data = JsonParser.parseString(body).asJsonObject

            val results = if (!data.get("results").isJsonNull) {
                data.get("results").asJsonObject.keySet().map {
                    val result = data.get("results").asJsonObject.get(it).asJsonObject
                    Gson().fromJson(result, Track::class.java)
                }
            } else {
                listOf()
            }

            response.close()
            flow.emit(results)
        }

        return flow
    }
}

enum class SearchState {
    SONG,
    ARTIST,
}
