package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    WARPS_TITLE,
    ADD_NEW_WARP,
    REMOVE_WARP,
    CANCEL,
    EMPTY_WARPS_LIST,
    DELETE,
    EDIT,
    COOLDOWN_ERROR_MESSAGE,
    DELAY_MESSAGE,
    PLURAL,
    ADD_WARP_TITLE,
    WARP_NAME_PLACEHOLDER,
    SELECT_ICON,
    SAVE_WARP,
    WARP_ERROR_MESSAGE,
    WARP_CONFIG_TITLE,
    CONFIG_DELAY_TITLE,
    CONFIG_DELAY_MESSAGE,
    CONFIG_DEFAULT_WAIT_VALUE,
    CONFIG_COOLDOWN_TITLE,
    CONFIG_COOLDOWN_MESSAGE,
    CONFIG_INPUT_SECONDS_PLACEHOLDER,
    CONFIG_DELAY_ERROR_MESSAGE,
    CONFIG_COOLDOWN_ERROR_MESSAGE,
    ;

    fun resource(): StringResource = StringResource("warps", this.name)
}