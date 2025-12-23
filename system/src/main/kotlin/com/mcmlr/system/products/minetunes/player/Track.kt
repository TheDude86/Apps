package com.mcmlr.system.products.minetunes.player

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("added_date") val addedDate: String,
    @SerializedName("album") val album: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("length") val length: Short,
    @SerializedName("plays") val plays: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("release_date_precision") val releaseDatePrecision: String,
    @SerializedName("song") val song: String,
)