package com.mcmlr.system.products.minetunes.nbs.data

import java.io.File

data class Song(
    val title: String,
    val nbsArtist: String,
    val artist: String,
    val description: String,
    val layersMap: Map<Int, Layer>,
    val height: Short,
    val length: Short,
    val speed: Float,
    val delay: Float,
    val customInstruments: List<CustomInstrument>,
    val firstCustomInstrumentIndex: Int,
    val isStereo: Boolean,
    val path: File,
)