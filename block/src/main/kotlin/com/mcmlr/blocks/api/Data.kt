package com.mcmlr.blocks.api

import org.bukkit.Location
import java.util.*

data class ScrollModel(val event: ScrollEvent)

data class CursorModel(val playerId: UUID, val data: Location, val event: CursorEvent)

data class FixedCursorModel(val playerId: UUID, val deltaX: Float, val y: Float, val event: CursorEvent)

enum class ScrollEvent {
    UP,
    DOWN,
    UPDATE,
}

enum class CursorEvent {
    MOVE,
    CLICK,
    CLEAR,
}
