package com.mcmlr.system.products.minetunes.nbs.data

data class Layer(val notesMap: MutableMap<Int, Note> = mutableMapOf(), var volume: Byte = 100, var panning: Int = 100, var name: String = "")