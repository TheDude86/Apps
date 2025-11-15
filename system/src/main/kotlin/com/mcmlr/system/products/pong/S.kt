package com.mcmlr.system.products.pong

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    PONG_TITLE,
    WIN_GOAL,
    LOSE_GOAL,
    WINNER,
    LOSER,
    DEFAULT_SCORE,
    SCORE_TEMPLATE,
    PLAY,
    ;

    fun resource(): StringResource = StringResource("pong", this.name)
}
