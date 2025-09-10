package com.mcmlr.system.products.landing

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.products.announcements.AnnouncementModel
import com.mcmlr.system.products.announcements.AnnouncementsRepository
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FeedBlock @Inject constructor(
    player: Player,
    origin: Location,
    announcementsRepository: AnnouncementsRepository,
): Block(player, origin) {
    private val view = FeedViewController(player = player, origin = origin)
    private val interactor = FeedInteractor(view, announcementsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun enableCTA(enabled: Boolean) {
        view.enableCTA = enabled
    }

    fun setCustomFeed(announcements: List<AnnouncementModel>) {
        interactor.updateFeed(announcements)
    }
}

class FeedViewController(
    var enableCTA: Boolean = true,
    private val player: Player,
    origin: Location,
): ViewController(player, origin), FeedPresenter {

    private lateinit var feed: ListFeedView

    override fun createView() {

        feed = addListFeedView(
            modifier = Modifier()
                .size(MATCH_PARENT, MATCH_PARENT)
                .center()
                .margins(start = 50, end = 50),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

    override fun setFeed(list: List<AnnouncementModel>) {
        feed.updateView {
            list.forEach {
                val height = if (it.ctaText != null) 350 else 300

                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, height)
                        .margins(start = 50, end = 50, top = 50, bottom = 50)
                ) {


                    val title = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .alignTopToTopOf(this)
                            .margins(top = 50, start = 50),
                        text = PlaceholderAPI.setPlaceholders(player, it.title),
                        size = 10
                    )

                    val author = Bukkit.getOfflinePlayer(UUID.fromString(it.authorId))
                    val head = ItemStack(Material.PLAYER_HEAD)
                    val headMeta = head.itemMeta as SkullMeta
                    headMeta.setOwningPlayer(author)
                    head.itemMeta = headMeta

                    val authorHead = addItemView(
                        modifier = Modifier()
                            .size(20, 20)
                            .alignStartToStartOf(title)
                            .alignTopToBottomOf(title)
                            .margins(top = 10),
                        item = head
                    )

                    val formatter = SimpleDateFormat("MMM d, yyyy")

                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(authorHead)
                            .alignTopToTopOf(authorHead)
                            .alignBottomToBottomOf(authorHead),
                        text = "${ChatColor.BOLD}${author.name} • ${ChatColor.RESET}${formatter.format(Date(it.timestamp))}",
                        size = 3,
                    )

                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT) //TODO: Support Match parent width
                            .alignStartToStartOf(authorHead)
                            .alignTopToBottomOf(authorHead)
                            .margins(top = 10),
                        text = PlaceholderAPI.setPlaceholders(player, it.message),
                        lineWidth = 280,
                        size = 5,
                        alignment = Alignment.LEFT,
                    )

                    if (it.ctaText != null) {
                        addButtonView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignBottomToBottomOf(this)
                                .alignEndToEndOf(this)
                                .margins(bottom = 50, end = 50),
                            text = "${ChatColor.GOLD}${it.ctaText}",
                            highlightedText = "${ChatColor.GOLD}${it.ctaText.bolden()}",
                            size = 5,
                        ) {
                            if (!enableCTA) return@addButtonView
                            val command = it.cta ?: return@addButtonView
                            player.performCommand(PlaceholderAPI.setPlaceholders(player, command))
                        }
                    }

//                    log(Log.ERROR, "Title Bottom=${title.bottom()} Message Top=${message.top()}")
//                    log(Log.DEBUG,
//                        "\nTop P=${message.getViewModifier().top?.p}\n" +
//                                "Top Pos=${message.getViewModifier().top?.view?.getPosition()?.y}\n" +
//                                "Top Margin=${message.getViewModifier().m.top}\n" +
//                                "Height=${message.getDimensions().height}"
//                    )

//                    addTextView(
//                        modifier = Modifier()
//                            .size(MATCH_PARENT, WRAP_CONTENT)
//                            .alignStartToStartOf(title)
//                            .alignTopToBottomOf(title)
//                            .margins(top = 50),
//                        text = it.message,
//                        size = 8
//                    )
                }
            }
        }
    }

}

interface FeedPresenter: Presenter {
    fun setFeed(list: List<AnnouncementModel>)
}

class FeedInteractor(
    private val presenter: FeedPresenter,
    private val announcementsRepository: AnnouncementsRepository,
): Interactor(presenter) {

    private var customFeed: List<AnnouncementModel>? = null

    override fun onCreate() {
        super.onCreate()

        val feed = customFeed ?: announcementsRepository.announcements()
        presenter.setFeed(feed)

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume(newOrigin: Location?) {
        super.onResume(newOrigin)
    }

    override fun onClose() {
        super.onClose()
    }

    fun updateFeed(announcements: List<AnnouncementModel>) {
        customFeed = announcements
        presenter.setFeed(announcements)
    }
}