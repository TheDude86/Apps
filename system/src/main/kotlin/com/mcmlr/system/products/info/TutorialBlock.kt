package com.mcmlr.system.products.info

import com.mcmlr.blocks.api.block.*
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
    origin: Location,
): Block(player, origin) {
    private val view = TutorialViewController(player, origin)
    private val interactor = TutorialInteractor(origin, player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TutorialViewController(
    private val player: Player,
    origin: Location,
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

    override fun setClockText(text: String) = clockTextView.setTextView(text)

    override fun setNextCTAListener(listener: () -> Unit) {
        nextCTACallback = listener
    }

    override fun setCursorPosition(x: Int, y: Int) {
        cursor?.setPositionView(x, y)
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Tutorial",
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
                    text = "${ChatColor.BOLD}The End",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "That's all for now!  We hope you enjoy this new way to use plugins.",
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}Finish ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Finish ➡",
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
                    text = "${ChatColor.BOLD}Apps",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "Opening ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} with ${ChatColor.BOLD}/.${ChatColor.RESET} will open the home screen where server staff can customize the title and message for players.  Also you can navigate to the Apps screen and explore all the apps available to you on the server.",
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
                    text = "Currently there are only a handful of 1st party apps since this is still a new project but as development progresses, more 1st party apps will be added as well as 3rd party app support and the developers of your favorite plugins will be able to integrate and display there apps on this screen as well.",
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
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
                    text = "${ChatColor.BOLD}Complex Elements",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "There are also a few more complex elements, here is the Carousel View and the Feed View.",
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
                        text = "Click on the first or last element to cycle through the carousel.",
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
                                        text = "Element $it",
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
                        text = "Scroll up and down while looking at at the feed to scroll through the content.",
                        size = 4,
                    )
                }


                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
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
                    text = "${ChatColor.BOLD}Live updates",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} is also very reactive so screens can update rapidly as you can see by the little cursor following where you're looking and by the clock below.",
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
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
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
                    text = "${ChatColor.BOLD}Interactive Elements",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "Text buttons aren't the only interactive element in ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET}.  There's also item buttons and text inputs, like the ones below.  Item buttons work the same as text buttons and text inputs need to be clicked like a button and the next line of text you send in chat will be used as your text input.  Try it out on the two below.",
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
                            demoItemText?.setTextView("${ChatColor.GREEN}Item button clicked $clicks time${if (clicks != 1) "s" else ""}")
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
                    text = "${ChatColor.GRAY}Click here to input text...",
                    highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Click here to input text...",
                    teleportDuration = 0,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
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
                    text = "${ChatColor.BOLD}Welcome to ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET}${ChatColor.BOLD}!",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = "${ChatColor.GOLD}${ChatColor.BOLD}Apps ${ChatColor.RESET}is an ambitious new project that is currently in beta. The goal is to provide a framework for plugin developers to display graphical user interfaces for their plugins instead of having to rely on players memorizing dozens of commands. I still have a lot of work to do before I achieve that goal but in the mean time, this plugin is currently a simple demo of how these apps can work in the future. Expect plenty of new updates and features in the near future!",
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
                    text = "As you can see, you can now display significantly more content rich GUIs for you and your players.  Also, along with text, you can also display items, blocks and entities in these GUIs.  Not only does ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} allow for displaying more complex content but it is also interactive.  Try looking at the \"${ChatColor.GOLD}Next ➡${ChatColor.RESET}\" button in the bottom right, you'll see it turns bold when you do.  This means the button is highlighted and left clicking your mouse will click that button!",
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
                    text = "Click on the \"${ChatColor.GOLD}Next ➡${ChatColor.RESET}\" button when you're ready to explore more features ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} offers.",
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToEndOf(this)
                        .alignBottomToBottomOf(this),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
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
    private val origin: Location,
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
                            text = "Element $index",
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
                    presenter.setClockText("${ChatColor.DARK_AQUA}You have been looking at this screen for $timer second${if (timer == 1) "" else "s"}.")
                }
                timer++

                delay(1.seconds)
            }
        }
    }

}