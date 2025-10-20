package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.block.EmptyContextListener
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.core.DudeDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Color
import kotlin.math.max

class PagerView(
    modifier: Modifier,
    background: Color,
    height: Int = 0,
): ViewContainer(modifier, false, background, height = height) {

    private var index = 0
    private lateinit var centerView: ViewContainer
    private lateinit var leftView: ViewContainer
    private lateinit var rightView: ViewContainer

    private var adapter: PagerViewAdapter? = null

    private val pageListeners: MutableList<PageListener> = mutableListOf()

    init {
        val dimensions = getDimensions()

        centerView = addViewContainer(
            modifier = Modifier()
                .size(dimensions.width / 3, dimensions.height)
                .center(),
            background = Color.fromARGB(0, 255, 0, 0),
            height = 4,
            clickable = true,
        )

        leftView = addViewContainer(
            modifier = Modifier()
                .size(dimensions.width / 3, dimensions.height)
                .alignEndToStartOf(centerView)
                .centerVertically(),
            background = Color.fromARGB(0, 0, 255, 0),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    rightView.updateView(EmptyContextListener<ViewContainer>())
                    centerView.height = 0
                    leftView.height = 4
                    centerView.setPositionView(dimensions.width * 2 / 3)

                    centerView.dudeDisplay?.setTeleportDuration(0)
                    leftView.dudeDisplay?.setTeleportDuration(0)
                    rightView.dudeDisplay?.setTeleportDuration(0)

                    CoroutineScope(Dispatchers.IO).launch {
                        delay(150)
                        CoroutineScope(DudeDispatcher()).launch {
                            index = if (index == 0) (adapter?.getCount() ?: 1) - 1 else index - 1
                            pageListeners.forEach { it.invoke(index) }
                            reset()
                        }
                    }
                }
            },
        )

        rightView = addViewContainer(
            modifier = Modifier()
                .size(dimensions.width / 3, dimensions.height)
                .alignStartToEndOf(centerView)
                .centerVertically(),
            background = Color.fromARGB(0, 0, 0, 255),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    leftView.updateView(EmptyContextListener<ViewContainer>())
                    centerView.height = 0
                    rightView.height = 4
                    centerView.setPositionView(- (dimensions.width * 2 / 3))

                    centerView.dudeDisplay?.setTeleportDuration(0)
                    leftView.dudeDisplay?.setTeleportDuration(0)
                    rightView.dudeDisplay?.setTeleportDuration(0)

                    CoroutineScope(Dispatchers.IO).launch {
                        delay(150)
                        CoroutineScope(DudeDispatcher()).launch {
                            index = if (index == (adapter?.getCount() ?: 1) - 1) 0 else index + 1
                            pageListeners.forEach { it.invoke(index) }
                            reset()
                        }
                    }
                }
            },
        )
    }

    fun addPagerListener(listener: PageListener) {
        pageListeners.add(listener)
    }

    fun attachAdapter(adapter: PagerViewAdapter) {
        this.adapter = adapter
        reset()
    }

    private fun reset() {
        centerView.setPositionView(0)
        centerView.setHeightView(4)
        leftView.setHeightView(0)
        rightView.setHeightView(0)

        centerView.dudeDisplay?.setTeleportDuration(3)
        leftView.dudeDisplay?.setTeleportDuration(3)
        rightView.dudeDisplay?.setTeleportDuration(3)

        adapter?.renderElement(false, circularIndex(index - 1), leftView)
        adapter?.renderElement(true, circularIndex(index + 1), rightView)
        adapter?.renderElement(false, circularIndex(index), centerView)
    }

    override fun render() {
        super.render()
        adapter?.renderElement(false, circularIndex(index - 1), leftView)
        adapter?.renderElement(true, circularIndex(index + 1), rightView)
        adapter?.renderElement(false, circularIndex(index), centerView)
    }

    private fun circularIndex(index: Int): Int {
        val elementSize = adapter?.getCount() ?: return 0
        val modIndex = index % max(1, elementSize)

        return if (modIndex < 0) elementSize + modIndex else modIndex
    }
}

abstract class PagerViewAdapter {

    abstract fun getCount(): Int

    abstract fun renderElement(selected: Boolean, index: Int, parent: ViewContainer)
}

interface PageListener {
    fun invoke(page: Int)
}
