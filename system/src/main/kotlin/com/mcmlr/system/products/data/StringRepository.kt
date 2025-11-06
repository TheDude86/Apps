package com.mcmlr.system.products.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader

open class StringRepository {

    private lateinit var strings: JsonObject

    fun loadStrings(path: String) {
        val lines = this::class.java.getResourceAsStream(path)?.reader()
        strings = JsonParser.parseReader(JsonReader(lines)).asJsonObject
    }

    fun getString(id: S) {
        strings.get(id.toString().lowercase())
    }
}

enum class S {
    FOO,
}
