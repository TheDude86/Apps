package com.mcmlr.system

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    HOME,
    APPS,
    FAVORITES,
    SPAWN,
    BACK,
    TELEPORTS,
    FINISH,
    SETUP_FINISH_PARAGRAPH_ONE,
    CREATE_FIRST_POST,
    SETUP_ANNOUNCEMENTS_PARAGRAPH,
    NEXT_ARROW,
    CREATE_POST,
    CUSTOMIZE_APPS,
    SETUP_CUSTOMIZE_APPS,
    CHOOSE_APPS,
    SETUP_ENABLE_APPS,
    CHECKBOX_ENABLED,
    CHECKBOX_DISABLED,
    SERVER_NAME,
    SETUP_SERVER_NAME_PARAGRAPH,
    SET_SERVER_TITLE,
    GETTING_STARTED,
    TUTORIAL,
    SETUP,
    SETUP_SUPPORT_PARAGRAPH,
    GET_SUPPORT_LINKS,
    DISCORD,
    MODRINTH,
    SPIGOT,
    ;

    fun resource(): StringResource = StringResource("system", this.name)
}