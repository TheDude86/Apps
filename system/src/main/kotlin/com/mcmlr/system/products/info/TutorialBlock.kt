package com.mcmlr.system.products.info

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.api.views.View.Companion.WRAP_CONTENT
import com.mcmlr.blocks.core.DudeDispatcher
import kotlinx.coroutines.*
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TutorialBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    private val view = TutorialViewController(player, origin)
    private val interactor = TutorialInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TutorialViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), TutorialPresenter {

    private lateinit var contentContainer: ViewContainer
    private lateinit var clockTextView: TextView
    private var demoItemText: TextView? = null
    private var pagerView: PagerView? = null

    private var cursor: ItemView? = null


    private var nextCTACallback: () -> Unit = {}

    override fun setAdapter(adapter: PagerViewAdapter) {
        pagerView?.attachAdapter(adapter)
    }

    override fun setClockText(text: String) = clockTextView.update(text = text)

    override fun setNextCTAListener(listener: () -> Unit) {
        nextCTACallback = listener
    }

    override fun setCursorPosition(x: Int, y: Int) {
        cursor?.updatePosition(x, y)
    }

    override fun createView() {
        super.createView()
        pagerView = null

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${R.getString(player, S.TUTORIAL_TITLE.resource())}",
            size = 16,
        )

        contentContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignStartToStartOf(this)
                .alignEndToEndOf(this)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(start = 600, top = 300, end = 600, bottom = 500),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

    override fun setContent(page: Int) {
        when (page) {
            1 -> pageOne()
            2 -> pageTwo()
            3 -> pageThree()
            4 -> pageFour()
            5 -> pageFive()
            6 -> pageSix()
        }
    }

    private fun pageSix() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_SIX_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_SIX_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.FINISH_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.FINISH_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            routeBack()
                        }
                    }
                )
            }
        })
    }

    private fun pageFive() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_FIVE_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_FIVE_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                val paragraphTwo = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(paragraphOne)
                        .alignStartToStartOf(paragraphOne)
                        .margins(top = 20),
                    text = R.getString(player, S.PAGE_FIVE_PARAGRAPH_TWO.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCTACallback.invoke()
                        }
                    }
                )
            }
        })
    }

    private fun pageFour() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_FOUR_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_FOUR_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                pagerView = addPagerView(
                    modifier = Modifier()
                        .size(500, 200)
                        .alignStartToStartOf(paragraphOne)
                        .alignTopToBottomOf(paragraphOne)
                        .margins(start = 500),
                )


                pagerView?.let {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(it)
                            .alignStartToStartOf(it)
                            .alignEndToEndOf(it),
                        text = R.getString(player, S.PAGE_FOUR_PAGER_VIEW_TEXT.resource()),
                        size = 4,
                    )

                    val feed = addListFeedView(
                        modifier = Modifier()
                            .size(300, 200)
                            .alignTopToTopOf(it)
                            .alignStartToEndOf(it)
                            .margins(start = 100),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                repeat(100) {
                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(this),
                                        text = R.getString(player, S.ELEMENT_INDEX.resource(), it),
                                        size = 4,
                                    )
                                }
                            }
                        }
                    )

                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(feed)
                            .alignStartToStartOf(feed)
                            .alignEndToEndOf(feed),
                        text = R.getString(player, S.PAGE_FOUR_FEED_VIEW_TEXT.resource()),
                        size = 4,
                    )
                }


                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCTACallback.invoke()
                        }
                    }
                )
            }
        })
    }

    private fun pageThree() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_THREE_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_THREE_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                clockTextView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(paragraphOne)
                        .alignStartToStartOf(paragraphOne)
                        .margins(top = 200),
                    text = "",
                    lineWidth = 500,
                )

                cursor = addItemView(
                    modifier = Modifier()
                        .size(10, 10)
                        .center(),
                    item = ItemStack(Material.SMOOTH_QUARTZ),
                    height = -2,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCTACallback.invoke()
                        }
                    }
                )
            }
        })
    }

    private fun pageTwo() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_TWO_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_TWO_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                var clicks = 0

                val demoItemButton = addItemButtonView(
                    modifier = Modifier()
                        .size(100, 100)
                        .alignStartToStartOf(paragraphOne)
                        .alignTopToBottomOf(paragraphOne)
                        .margins(start = 500, top = 100),
                    item = ItemStack(Material.DIAMOND),
                    callback = object : Listener {
                        override fun invoke() {
                            clicks++
                            val plural = if (clicks != 1) R.getString(player, S.PLURAL.resource()) else ""
                            val message = R.getString(player, S.DEMO_ITEM_BUTTON_TEXT.resource(), clicks, plural)
                            demoItemText?.update(text = message)
                        }
                    }
                )

                demoItemText = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(demoItemButton)
                        .alignStartToStartOf(demoItemButton)
                        .alignEndToEndOf(demoItemButton),
                    text = "",
                    teleportDuration = 0,
                    size = 4,
                )

                addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(demoItemButton)
                        .alignTopToTopOf(demoItemButton)
                        .alignBottomToBottomOf(demoItemButton)
                        .margins(start = 200),
                    text = "${ChatColor.GRAY}${R.getString(player, S.INPUT_TEXT_PLACEHOLDER.resource())}",
                    highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}${R.getString(player, S.INPUT_TEXT_PLACEHOLDER.resource())}",
                    teleportDuration = 0,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCTACallback.invoke()
                        }
                    }
                )
            }
        })
    }

    private fun pageOne() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = R.getString(player, S.PAGE_ONE_TITLE.resource()),
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_ONE_PARAGRAPH_ONE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                val paragraphTwo = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(paragraphOne)
                        .alignStartToStartOf(paragraphOne)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_ONE_PARAGRAPH_TWO.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(paragraphTwo)
                        .alignStartToStartOf(paragraphTwo)
                        .margins(top = 50),
                    text = R.getString(player, S.PAGE_ONE_PARAGRAPH_THREE.resource()),
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCTACallback.invoke()
                        }
                    }
                )
            }
        })
    }

}

interface TutorialPresenter: Presenter {
    fun setContent(page: Int)

    fun setNextCTAListener(listener: () -> Unit)

    fun setCursorPosition(x: Int, y: Int)

    fun setClockText(text: String)

    fun setAdapter(adapter: PagerViewAdapter)
}

class TutorialInteractor(
    private val player: Player,
    private val presenter: TutorialPresenter,
//    private val cursorRepository: CursorRepository,
): Interactor(presenter) {

    private var clockJob: Job? = null
    private var currentPage = 1
    private var timer = 0

    override fun onCreate() {
        super.onCreate()

//        TODO: Reimplement cursor tracking
//        cursorRepository.cursorStream(player.uniqueId)
//            .filter { it.event != CursorEvent.CLEAR }
//            .collectOn(Dispatchers.IO)
//            .collectLatest { model ->
//                val originYaw = origin.yaw
//                val currentYaw = model.data.yaw
//
//                val yawDelta = if (originYaw > 90f && currentYaw < -90f) {
//                    (originYaw - 180) - (180 + currentYaw)
//                } else if (originYaw < -90f && currentYaw > 90f) {
//                    (180 + originYaw) + (180 - currentYaw)
//                } else {
//                    originYaw - currentYaw
//                }
//
//                val modifier = max(-58.8f, min(58.8f, yawDelta))
//                val radian = 0.01745329 * modifier
//                val finalX = (-1162.79 * tan(radian)).toInt()
//
//                val maxPitch = -(modifier / 14.026f).pow(2) + 43f
//                val rotation = 0.01745329 * max(-maxPitch, min(maxPitch, model.data.pitch))
//                val range = -1080.0 / tan(0.01745329 * maxPitch)
//                val newY = 75 + (range * tan(rotation)).toInt()
//                val finalY = min(1165, max(-1000, newY))
//
//                CoroutineScope(DudeDispatcher()).launch {
//                    presenter.setCursorPosition(finalX, finalY)
//                }
//            }
//            .disposeOn(disposer = this)

        currentPage = 1

        presenter.setContent(currentPage)

        presenter.setNextCTAListener {
            currentPage++
            presenter.setContent(currentPage)
            clock(currentPage)
            adapter(currentPage)
        }
    }

    private fun adapter(page: Int) {
        if (page != 4) return

        presenter.setAdapter(object : PagerViewAdapter() {
            override fun getCount(): Int = 10

            override fun renderElement(selected: Boolean, index: Int, parent: ViewContainer) {
                parent.updateView(object : ContextListener<ViewContainer>() {
                    override fun ViewContainer.invoke() {
                        addTextView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .center(),
                            text = R.getString(player, S.ELEMENT_INDEX.resource(), index),
                            size = 4,
                        )
                    }
                })
            }
        })
    }

    private fun clock(page: Int) {
        if (page != 3) {
            clockJob?.cancel()
            return
        }

        clockJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                CoroutineScope(DudeDispatcher()).launch {
                    val string = if (timer == 1) "" else R.getString(player, S.PLURAL.resource())
                    val message = R.getString(player, S.TIMER_TEXT.resource(), timer, string)
                    presenter.setClockText("${ChatColor.DARK_AQUA}$message")
                }
                timer++

                delay(1.seconds)
            }
        }
    }

}