package com.mcmlr.blocks.api.views

import org.bukkit.entity.TextDisplay.TextAlignment

data class Dimensions(val width: Int, val height: Int)

data class Coordinates(val x: Int, val y: Int) {
    fun offset(position: Coordinates): Coordinates = Coordinates(x + position.x, y + position.y)
}

data class Edge(val p: Int, val view: Viewable, val alignment: EdgeType)

class Modifier {
    var width = 0
    var height = 0
    var xType = PositionType.ABSOLUTE
    var yType = PositionType.ABSOLUTE
    var x = 0
    var y = 0
    var start: Edge? = null
    var top: Edge? = null
    var end: Edge? = null
    var bottom: Edge? = null
    var m = Margins()

    fun size(width: Int, height: Int): Modifier {
        this.width = width
        this.height = height
        return this
    }

    fun centerHorizontally(): Modifier {
        this.xType = PositionType.CENTERED
        return this
    }

    fun centerVertically(): Modifier {
        this.yType = PositionType.CENTERED
        return this
    }

    fun center(): Modifier {
        this.xType = PositionType.CENTERED
        this.yType = PositionType.CENTERED
        return this
    }

    fun position(x: Int, y: Int): Modifier {
        this.xType = PositionType.ABSOLUTE
        this.yType = PositionType.ABSOLUTE
        this.x = x
        this.y = y
        return this
    }

    fun x(x: Int): Modifier {
        this.xType = PositionType.ABSOLUTE
        this.x = x
        return this
    }

    fun y(y: Int): Modifier {
        this.yType = PositionType.ABSOLUTE
        this.y = y
        return this
    }

    fun margins(
        start: Int = 0,
        top: Int = 0,
        end: Int = 0,
        bottom: Int = 0,
    ): Modifier {
        this.m = Margins(start, top, end, bottom)
        return this
    }

    fun alignStartToStartOf(view: Viewable): Modifier {
        xType = PositionType.ALIGNED
        start = Edge(view.start(), view, EdgeType.START)
        return this
    }

    fun alignStartToEndOf(view: Viewable): Modifier {
        xType = PositionType.ALIGNED
        start = Edge(view.end(), view, EdgeType.END)
        return this
    }

    fun alignTopToTopOf(view: Viewable): Modifier {
        yType = PositionType.ALIGNED
        top = Edge(view.top(), view, EdgeType.TOP)
        return this
    }

    fun alignTopToBottomOf(view: Viewable): Modifier {
        yType = PositionType.ALIGNED
        top = Edge(view.bottom(), view, EdgeType.BOTTOM)
        return this
    }

    fun alignEndToStartOf(view: Viewable): Modifier {
        xType = PositionType.ALIGNED
        end = Edge(view.start(), view, EdgeType.START)
        return this
    }

    fun alignEndToEndOf(view: Viewable): Modifier {
        xType = PositionType.ALIGNED
        end = Edge(view.end(), view, EdgeType.END)
        return this
    }

    fun alignBottomToTopOf(view: Viewable): Modifier {
        yType = PositionType.ALIGNED
        bottom = Edge(view.top(), view, EdgeType.TOP)
        return this
    }

    fun alignBottomToBottomOf(view: Viewable): Modifier {
        yType = PositionType.ALIGNED
        bottom = Edge(view.bottom(), view, EdgeType.BOTTOM)
        return this
    }

    fun updateAlignment() {
        start = updateEdge(start)
        top = updateEdge(top)
        end = updateEdge(end)
        bottom = updateEdge(bottom)
    }

    private fun updateEdge(edge: Edge?): Edge? {
        val edge = edge ?: return null
        return when (edge.alignment) {
            EdgeType.START -> Edge(edge.view.start(), edge.view, EdgeType.START)
            EdgeType.TOP -> Edge(edge.view.top(), edge.view, EdgeType.TOP)
            EdgeType.END -> Edge(edge.view.end(), edge.view, EdgeType.END)
            EdgeType.BOTTOM -> Edge(edge.view.bottom(), edge.view, EdgeType.BOTTOM)
        }
    }
}

enum class Axis { X, Y }

enum class Area { WIDTH, HEIGHT }

enum class PositionType {
    ALIGNED,
    ABSOLUTE,
    CENTERED,
}

enum class EdgeType {
    START,
    TOP,
    END,
    BOTTOM
}

enum class Alignment(val textAlignment: TextAlignment) {
    LEFT(TextAlignment.LEFT),
    CENTER(TextAlignment.CENTER),
    RIGHT(TextAlignment.RIGHT),
}

data class Margins(
    val start: Int = 0,
    val top: Int = 0,
    val end: Int = 0,
    val bottom: Int = 0,
)