package com.mcmlr.system.products.kits

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    KITS_TITLE,
    KIT_CONTENTS_ROW_NAME,
    KIT_NAME,
    KIT_PRICE,
    KIT_COOLDOWN,
    KIT_DESCRIPTION,
    GET_KIT,
    CREATE_KIT,
    EDIT_KIT,
    UPDATE_KIT,
    DELETE_KIT,
    CLAIMED_KIT_ERROR,
    KIT_COOLDOWN_ERROR,
    KIT_MONEY_ERROR,
    SINGLE_USE,
    KIT_CLAIMED,
    AVAILABLE_NOW,
    AVAILABLE_IN,
    DAYS_INPUT,
    HOURS_INPUT,
    MINUTES_INPUT,
    SECONDS_INPUT_SENTENCE,
    SECONDS_INPUT,
    SET_KIT_ICON,
    CREATE_KIT_TITLE,
    SET_KIT_NAME,
    SET_KIT_PRICE,
    SET_KIT_COOLDOWN,
    SET_KIT_COOLDOWN_SUBTITLE,
    SET_KIT_DESCRIPTION,
    KIT_CONTENTS_TITLE,
    ADD_ITEM_BUTTON,
    ADD_COMMAND_BUTTON,
    ;

    fun resource(): StringResource = StringResource("kits", this.name)
}