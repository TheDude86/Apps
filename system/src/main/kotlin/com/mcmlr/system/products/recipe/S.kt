package com.mcmlr.system.products.recipe

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    RECIPES_TITLE,
    SEARCH_PLACEHOLDER,
    ;

    fun resource(): StringResource = StringResource("recipes", this.name)
}