package com.mcmlr.system.products.spawn

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    SPAWN_TITLE,
    SPAWN_BUTTON,
    BACK_BUTTON,
    ;

    fun resource(): StringResource = StringResource("spawn", this.name)
}