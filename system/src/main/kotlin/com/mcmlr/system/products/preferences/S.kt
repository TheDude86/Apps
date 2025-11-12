package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    PREFERENCES_TITLE,
    FAVORITE_APPS_TITLE,
    FAVORITE_APPS_SUBTITLE,
    SELECT_FAVORITE_TITLE,
    REMOVE_BUTTON,
    ;

    fun resource(): StringResource = StringResource("preferences", this.name)
}
