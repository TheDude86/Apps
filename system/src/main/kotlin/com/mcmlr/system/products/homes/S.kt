package com.mcmlr.system.products.homes

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    HOMES,
    ADD_NEW_HOME,
    REMOVE_HOME,
    CANCEL,
    EMPTY_HOMES_MESSAGE,
    ;

    fun resource(): StringResource = StringResource("homes", this.name)
}