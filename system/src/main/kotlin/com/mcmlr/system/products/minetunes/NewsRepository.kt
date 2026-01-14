package com.mcmlr.system.products.minetunes

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration.Companion.hours

object NewsRepository {
    private const val FETCH_NEWS_DELAY = 8

    lateinit var news: NewsResponse

    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                fetchNews().collectFirst {
                    news = it
                }

                delay(FETCH_NEWS_DELAY.hours)
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://uploadplaylist-l5cijtvgrq-uc.a.run.app"
            val model = Gson().toJson(playlist)

            val request = Request.Builder().url("$url/?data=$model").build()
            OkHttpClient().newCall(request).execute()
        }
    }

    fun updateNews(model: NewsModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://uploadnews-l5cijtvgrq-uc.a.run.app"
            val model = Gson().toJson(model)

            val request = Request.Builder().url("$url/?data=$model").build()
            OkHttpClient().newCall(request).execute()
        }
    }

    fun fetchNews(): Flow<NewsResponse> {
        val flow = MutableSharedFlow<NewsResponse>()

        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://fetchnews-l5cijtvgrq-uc.a.run.app/"

            val request = Request.Builder().url(url).build()
            val response = OkHttpClient().newCall(request).execute() //TODO: Move OkHTTPClient to dependency
            val body = response.body?.string() ?: return@launch
            val data = JsonParser.parseString(body).asJsonObject

            val results = data.get("news").asJsonObject.keySet().map {
                val result = data.get("news").asJsonObject.get(it).asJsonObject
                Gson().fromJson(result, NewsModel::class.java)
            }

            response.close()
            flow.emit(NewsResponse(results))
        }

        return flow
    }

}

data class NewsResponse(
    val news: List<NewsModel>,
)

data class NewsModel(
    val title: String,
    val message: String,
    val cta: String,
    val badge: String,
    val backgroundColor: Int,
    val highlightBackgroundColor: Int,
    val action: NewsActionModel,
    val date: Long = 0,
)

data class NewsActionModel(
    val type: NewsActionType,
    val data: String,
)

enum class NewsActionType {
    TUTORIAL,
    PLAYLIST,
}
