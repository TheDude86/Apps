package com.mcmlr.system.products.announcements

import com.mcmlr.system.products.announcements.AnnouncementSelectorBlock.Companion.ANNOUNCEMENT_SELECT_BUNDLE_KEY
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AnnouncementSelectorBlock @Inject constructor(
    player: Player,
    origin: Location,
    announcementsRepository: AnnouncementsRepository,
): Block(player, origin) {
    companion object {
        const val ANNOUNCEMENT_SELECT_BUNDLE_KEY = "announcement select"
    }

    private val view = AnnouncementSelectorViewController(player, origin)
    private val interactor = AnnouncementSelectorInteractor(view, announcementsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AnnouncementSelectorViewController(
    player: Player,
    origin: Location,
): NavigationViewController(player, origin), AnnouncementSelectorPresenter {

    private lateinit var feed: ListFeedView

    override fun setFeed(list: List<AnnouncementModel>, selectedPostCallback: (UUID) -> Unit) {
        feed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                list.forEach {
                    val height = if (it.ctaText != null) 350 else 300

                    val postView = addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, height)
                            .margins(start = 50, end = 50, top = 50, bottom = 50),
                        clickable = true,
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val title = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .alignTopToTopOf(this)
                                        .margins(top = 50, start = 50),
                                    text = it.title,
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

                                val message = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT) //TODO: Support Match parent width
                                        .alignStartToStartOf(authorHead)
                                        .alignTopToBottomOf(authorHead)
                                        .margins(top = 10),
                                    text = it.message,
                                    lineWidth = 280,
                                    size = 5,
                                    alignment = Alignment.LEFT,
                                )

                                if (it.ctaText != null) {
                                    addButtonView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignTopToBottomOf(message)
                                            .alignEndToEndOf(this)
                                            .margins(top = 20, end = 50),
                                        text = "${ChatColor.GOLD}${it.ctaText}",
                                        highlightedText = "${ChatColor.GOLD}${it.ctaText.bolden()}",
                                        size = 5,
                                    )
                                }
                            }
                        }
                    )

                    postView.addListener(object : Listener {
                        override fun invoke() {
                            selectedPostCallback.invoke(it.id)
                        }
                    })
                }
            }
        })
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Select Post to Edit",
            size = 16,
        )

        feed = addListFeedView(
            modifier = Modifier()
                .size(800, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 50, bottom = 300),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }
}

interface AnnouncementSelectorPresenter: Presenter {
    fun setFeed(list: List<AnnouncementModel>, selectedPostCallback: (UUID) -> Unit)
}

class AnnouncementSelectorInteractor(
    private val presenter: AnnouncementSelectorPresenter,
    private val announcementsRepository: AnnouncementsRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setFeed(announcementsRepository.announcements()) { announcementId ->
            val announcement = announcementsRepository.getAnnouncements(announcementId) ?: return@setFeed
            addBundleData(ANNOUNCEMENT_SELECT_BUNDLE_KEY, announcement)
            routeBack()
        }
    }
}
