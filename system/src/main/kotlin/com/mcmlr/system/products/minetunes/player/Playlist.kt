package com.mcmlr.system.products.minetunes.player

data class Playlist(
    val uuid: String? = null,
    var icon: Icon? = null,
    var name: String? = null,
    var favorite: Boolean? = false,
    val createdDate: Long = 0L,
    var lastUsedDate: Long = 0L,
    val songs: MutableList<Track> = mutableListOf()
)

data class Icon(
    val type: IconType,
    val data: String
)

enum class IconType {
    MATERIAL,
    HEAD,
}
