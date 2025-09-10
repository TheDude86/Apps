package com.mcmlr.apps.app.block.data

class Bundle {
    val map = HashMap<String, Any>()

    fun clear() = map.clear()

    fun add(key: String, data: Any) {
        map[key] = data
    }

    inline fun <reified T> getData(key: String): T? {
        val datum = map[key] ?: return null
        (datum as? T)?.let {
            return it
        } ?: return null
    }
}