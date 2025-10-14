package com.mcmlr.system.products.announcements

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.colorize
import com.mcmlr.blocks.core.titlecase
import com.mcmlr.system.placeholder.placeholders
import com.mcmlr.system.products.support.TextEditorBlock
import com.mcmlr.system.products.support.TextEditorBlock.Companion.TEXT_BUNDLE_KEY
import com.mcmlr.system.products.support.TextModel
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AnnouncementEditorBlock @Inject constructor(
    player: Player,
    camera: Camera,
    announcementsRepository: AnnouncementsRepository,
    textEditorBlock: TextEditorBlock,
): Block(player, camera) {
    companion object {
        const val ANNOUNCEMENT_BUNDLE_KEY = "announcement"
        const val ANNOUNCEMENT_POST_BUNDLE_KEY = "announcement"
    }

    private val view = AnnouncementEditorViewController(player, camera)
    private val interactor = AnnouncementEditorInteractor(player, view, announcementsRepository, textEditorBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setSavePost(savePost: Boolean) {
        interactor.savePost = savePost
    }

    fun setSelectedAnnouncement(announcementModel: AnnouncementModel?) {
        interactor.builder = announcementModel?.toBuilder() ?: AnnouncementModel.Builder()
        interactor.isUpdating = announcementModel != null
    }
}

class AnnouncementEditorViewController(
    private val player: Player,
    camera: Camera,
): NavigationViewController(player, camera), AnnouncementEditorPresenter {

    private lateinit var editorFeed: FeedView
    private lateinit var buttonsContainer: ViewContainer
    private lateinit var titleView: TextInputView
    private lateinit var messageView: ButtonView
    private lateinit var enableCTAView: ButtonView
    private lateinit var createPostView: ButtonView
    private lateinit var errorMessageView: TextView
    private lateinit var title: TextView
    private var deletePostView: ButtonView? = null
    private var ctaTextView: TextInputView? = null
    private var ctaActionView: TextInputView? = null
    private var ctaActionTypeView: ButtonView? = null

    private var titleViewCallback: (String) -> Unit = {}
    private var ctaTextViewCallback: (String) -> Unit = {}
    private var ctaActionViewCallback: (String) -> Unit = {}
    private var ctaActionTypeCallback: () -> Unit = {}
    private var messageViewCallback: () -> Unit = {}
    private var enabledCTACallback: () -> Unit = {}
    private var deletePostCallback: () -> Unit = {}

    override fun setErrorMessage(message: String) {
        errorMessageView.setTextView(message)
    }

    override fun setTitleText(text: String) {
        titleView.setTextView(text)
    }

    override fun setMessageText(text: String) {
        messageView.setTextView(text)
    }

    override fun setCTAActionType(text: String) {
        ctaActionTypeView?.setTextView(text)
    }

    override fun setCTAText(text: String) {
        ctaTextView?.setTextView(text)
    }

    override fun setCreatePostListener(listener: () -> Unit) {
        createPostView.addListener(listener)
    }

    override fun setDeletePostListener(listener: () -> Unit) {
        deletePostCallback = listener
    }

    override fun setTitleListener(listener: (String) -> Unit) {
        titleViewCallback = listener
    }

    override fun setMessageListener(listener: () -> Unit) {
        messageViewCallback = listener
    }

    override fun setCTATextListener(listener: (String) -> Unit) {
        ctaTextViewCallback = listener
    }

    override fun setCTAActionListener(listener: (String) -> Unit) {
        ctaActionViewCallback = listener
    }

    override fun setCTAActionTypeListener(listener: () -> Unit) {
        ctaActionTypeCallback = listener
    }

    override fun setEnableCTAListener(listener: () -> Unit) {
        enabledCTACallback = listener
    }

    override fun createView() {
        super.createView()
        title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Create Post",
            size = 16,
        )

        editorFeed = addFeedView(
            modifier = Modifier()
                .size(1200, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 100, bottom = 500),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        buttonsContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, 200)
                .alignTopToBottomOf(editorFeed)
                .centerHorizontally()
                .margins(top = 100),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
            createPostView = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally(),
                text = "${ChatColor.GOLD}Create Post",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create Post",
                background = Color.fromARGB(0, 0, 0, 0)
            )

            errorMessageView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(createPostView)
                    .centerHorizontally()
                    .margins(top = 50),
                text = "",
                size = 4,
            )
        }
    }

    override fun setIsUpdating(isUpdating: Boolean) {
        val titleText = if (isUpdating) "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Create Post" else "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Update Post"
        title.setTextView(titleText)

        buttonsContainer.updateView {
            if (isUpdating) {
                createPostView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .x(-400)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.GOLD}Update Post",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Update Post",
                )

                deletePostView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .x(400)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.RED}Delete Post",
                    highlightedText = "${ChatColor.RED}${ChatColor.BOLD}Delete Post",
                ) {
                    deletePostCallback.invoke()
                }

                errorMessageView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(createPostView)
                        .centerHorizontally()
                        .margins(top = 50),
                    text = "",
                    size = 4,
                )
            } else {
                createPostView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .centerHorizontally(),
                    text = "${ChatColor.GOLD}Create Post",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create Post",
                )

                errorMessageView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(createPostView)
                        .centerHorizontally()
                        .margins(top = 50),
                    text = "",
                    size = 4,
                )
            }
        }
    }

    override fun setEditorFeed(model: AnnouncementModel.Builder, ctaEnabled: Boolean) {
        editorFeed.updateView {
            val postTitleTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .alignStartToStartOf(this)
                    .margins(start = 100),
                size = 6,
                text = "Post Title",
            )

            val postTitleMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(postTitleTitle)
                    .alignStartToStartOf(postTitleTitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}The title of your post. This support's color codes and Placeholder API.",
            )

            val titleText = (model.title ?: "Set post's title").placeholders(player).colorize()
            titleView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(postTitleTitle)
                    .alignBottomToTopOf(postTitleMessage),
                size = 6,
                text = "${ChatColor.GOLD}$titleText",
                highlightedText = "${ChatColor.GOLD}${titleText.bolden()}",
            )

            titleView.addTextChangedListener {
                titleViewCallback.invoke(it)
            }

            val postMessageTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(postTitleMessage)
                    .alignStartToStartOf(postTitleMessage)
                    .margins(top = 100),
                size = 6,
                text = "Post Message",
            )

            val postMessageMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(postMessageTitle)
                    .alignStartToStartOf(postMessageTitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}The message of your post. This will open a custom editor so you can write messages longer than Minecraft's chat limit.",
            )

            val messageText = (model.message?.toMCFormattedText() ?: "${ChatColor.GOLD}Set post's message").placeholders(player).colorize()
            messageView = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(postMessageTitle)
                    .alignBottomToTopOf(postMessageMessage),
                size = 6,
                maxLength = 1000,
                text = messageText,
                highlightedText = messageText.bolden(),
            ) {
                messageViewCallback.invoke()
            }

            val enableCTATitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(postMessageMessage)
                    .alignStartToStartOf(postMessageMessage)
                    .margins(top = 100),
                size = 6,
                text = "Add a CTA",
            )

            val enableCTAMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(enableCTATitle)
                    .alignStartToStartOf(enableCTATitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}Add a \"Call to Action\" button to your post. This button will appear in the bottom right of your post and will run the command you set in this editor when clicked. You can also customize the CTA's message.",
            )

            enableCTAView = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(enableCTATitle)
                    .alignBottomToTopOf(enableCTAMessage),
                size = 6,
                text = "${ChatColor.GOLD}${if (ctaEnabled) "On" else "Off"}",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${if (ctaEnabled) "On" else "Off"}",
            ) {
                enabledCTACallback.invoke()
            }

            if (ctaEnabled) {
                val ctaTextTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(enableCTAMessage)
                        .alignStartToStartOf(enableCTAMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "CTA Text",
                )

                val ctaTextMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(ctaTextTitle)
                        .alignStartToStartOf(ctaTextTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}Set the CTA's text. This support's color codes and Placeholder API.",
                )

                val ctaText = (model.ctaText ?: "Set post's CTA text").placeholders(player).colorize()
                ctaTextView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(ctaTextTitle)
                        .alignBottomToTopOf(ctaTextMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}$ctaText",
                    highlightedText = "${ChatColor.GOLD}${ctaText.bolden()}",
                )

                ctaTextView?.addTextChangedListener {
                    ctaTextViewCallback.invoke(it)
                }

                val ctaActionTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(ctaTextMessage)
                        .alignStartToStartOf(ctaTextMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "CTA Command",
                )

                val ctaActionMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(ctaActionTitle)
                        .alignStartToStartOf(ctaActionTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}Add a command that will run when a player clicks the CTA. This support's Placeholder API",
                )

                val ctaActionText = model.cta ?: "Set post's CTA command"
                ctaActionView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(ctaActionTitle)
                        .alignBottomToTopOf(ctaActionMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}$ctaActionText",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$ctaActionText",
                )

                ctaActionView?.addTextChangedListener(ctaActionViewCallback)

                val ctaActionTypeTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(ctaActionMessage)
                        .alignStartToStartOf(ctaActionMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "CTA Command Runner",
                )

                val ctaActionTypeMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(ctaActionTypeTitle)
                        .alignStartToStartOf(ctaActionTypeTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 310,
                    size = 4,
                    text = "${ChatColor.GRAY}Choose who will run the CTA command when pressed. You can choose between the player who clicked on the CTA or the server. Choosing the server will let you run a command even if the player doesn't have permission to do so.",
                )

                val ctaActionTypeText = model.ctaType.name.titlecase()
                ctaActionTypeView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(ctaActionTypeTitle)
                        .alignBottomToTopOf(ctaActionTypeMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}$ctaActionTypeText",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$ctaActionTypeText",
                ) {
                    ctaActionTypeCallback.invoke()
                }
            }
        }
    }

}

interface AnnouncementEditorPresenter: Presenter {
    fun setEditorFeed(model: AnnouncementModel.Builder, ctaEnabled: Boolean = false)
    fun setTitleText(text: String)
    fun setMessageText(text: String)
    fun setCTAText(text: String)
    fun setCTAActionType(text: String)
    fun setErrorMessage(message: String)
    fun setIsUpdating(isUpdating: Boolean)

    fun setTitleListener(listener: (String) -> Unit)
    fun setCTATextListener(listener: (String) -> Unit)
    fun setCTAActionListener(listener: (String) -> Unit)
    fun setMessageListener(listener: () -> Unit)
    fun setCTAActionTypeListener(listener: () -> Unit)
    fun setEnableCTAListener(listener: () -> Unit)
    fun setCreatePostListener(listener: () -> Unit)
    fun setDeletePostListener(listener: () -> Unit)
}

class AnnouncementEditorInteractor(
    private val player: Player,
    private val presenter: AnnouncementEditorPresenter,
    private val announcementsRepository: AnnouncementsRepository,
    private val textEditorBlock: TextEditorBlock,
): Interactor(presenter) {

    var builder = AnnouncementModel.Builder()
    var isUpdating = false
    var savePost = true

    private var ctaEnabled = false

    override fun onCreate() {
        super.onCreate()

        presenter.setEditorFeed(builder)
        presenter.setIsUpdating(isUpdating)
        if (builder.authorId == null) builder.authorId = player.uniqueId.toString()

        presenter.setCreatePostListener {
            val errorMessage = when {
                builder.title == null -> "You need to set a title first!"
                builder.message == null -> "You need to set a message first!"
                ctaEnabled && builder.ctaText == null -> "You need to set the CTA text first or disable the CTA!"
                ctaEnabled && builder.cta == null -> "You need to set the CTA action first or disable the CTA!"
                else -> null
            }

            if (errorMessage == null) {
                val announcement = builder.build() ?: return@setCreatePostListener

                if (savePost) {
                    if (isUpdating) {
                        announcementsRepository.updateAnnouncement(announcement)
                    } else {
                        announcementsRepository.addAnnouncement(announcement)
                    }
                    addBundleData(AnnouncementEditorBlock.ANNOUNCEMENT_BUNDLE_KEY, if (isUpdating) AnnouncementEditorResult.UPDATE else AnnouncementEditorResult.CREATE)
                } else {
                    addBundleData(AnnouncementEditorBlock.ANNOUNCEMENT_POST_BUNDLE_KEY, announcement)
                }

                routeBack()
            } else {
                presenter.setErrorMessage("${ChatColor.RED}$errorMessage")
            }
        }

        presenter.setDeletePostListener {
            val announcement = builder.build() ?: return@setDeletePostListener
            announcementsRepository.removeAnnouncement(announcement)
            addBundleData(AnnouncementEditorBlock.ANNOUNCEMENT_BUNDLE_KEY, AnnouncementEditorResult.DELETE)
            routeBack()
        }

        presenter.setTitleListener {
            val title = it.placeholders(player).colorize()
            presenter.setTitleText(title)
            builder.title = title
        }

        presenter.setMessageListener {
            textEditorBlock.setTextModel(builder.message)
            routeTo(textEditorBlock) { bundle ->
                val model = bundle.getData<TextModel>(TEXT_BUNDLE_KEY) ?: return@routeTo
                builder.message = model
                presenter.setEditorFeed(builder, ctaEnabled)
            }
        }

        presenter.setCTATextListener {
            val message = it.placeholders(player).colorize()
            builder.ctaText = message
            presenter.setCTAText(message)
        }

        presenter.setCTAActionListener {
            builder.cta = it
        }

        presenter.setCTAActionTypeListener {
            val newType = AnnouncementCTAType.entries[((builder.ctaType.ordinal) + 1) % AnnouncementCTAType.entries.size]
            builder.ctaType = newType
            presenter.setCTAActionType("${ChatColor.GOLD}${newType.toString().titlecase()}")
        }

        presenter.setEnableCTAListener {
            ctaEnabled = !ctaEnabled
            presenter.setEditorFeed(builder, ctaEnabled)
        }
    }
}

enum class AnnouncementEditorResult {
    CREATE,
    UPDATE,
    DELETE,
}
