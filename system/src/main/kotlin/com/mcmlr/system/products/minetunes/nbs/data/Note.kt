package com.mcmlr.system.products.minetunes.nbs.data

data class Note(val instrument: Byte, val key: Byte, val velocity: Byte, val panning: Int, val pitch: Short)