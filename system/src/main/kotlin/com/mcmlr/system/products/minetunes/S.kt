package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    MINE_TUNES_TITLE,
    HOME_BUTTON,
    MUSIC_BUTTON,
    SEARCH_BUTTON,
    ;

    fun resource(): StringResource = StringResource("mine tunes", this.name)
}