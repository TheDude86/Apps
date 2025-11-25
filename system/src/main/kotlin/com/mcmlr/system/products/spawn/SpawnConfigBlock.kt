package com.mcmlr.system.products.spawn

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.products.kits.KitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SpawnConfigBlock @Inject constructor(
    player: Player,
    origin: Origin,
    spawnRepository: SpawnRepository,
    kitRepository: KitRepository,
) : Block(player, origin) {
    private val view: SpawnConfigViewController = SpawnConfigViewController(player, origin)
    private val interactor: SpawnConfigInteractor = SpawnConfigInteractor(player, view, spawnRepository, kitRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class SpawnConfigViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
    SpawnConfigPresenter {

    private lateinit var contentView: ViewContainer
    private lateinit var enableSpawnButton: ButtonView
    private lateinit var setSpawnLocationView: ButtonView
    private lateinit var setWelcomeMessageView: TextInputView
    private lateinit var setFirstTimeKitView: ButtonView
    private lateinit var setRespawnLocationOrderView: ButtonView
    private lateinit var setSpawnOnJoinView: ButtonView
    private lateinit var joinServerView: TextInputView
    private lateinit var quitServerView: TextInputView
    private lateinit var cooldownView: TextInputView
    private lateinit var delayView: TextInputView
    private lateinit var messageView: TextView

    private lateinit var priorityCallback: (RespawnType, PriorityDirection) -> Unit
    private lateinit var enableCallback: (RespawnType, Boolean) -> Unit

    private var kitTitleView: TextView? = null
    private var kitSelectButton: ButtonView? = null
    private var captureButton: ButtonView? = null
    private var confirmLocationButton: ButtonView? = null
    private var tryAgainButton: ButtonView? = null
    private var cancelButton: ButtonView? = null
    private var kitsPager: PagerView? = null

    override fun setEnableSpawnListener(listener: Listener) = enableSpawnButton.addListener(listener)
    override fun setSpawnLocationListener(listener: Listener) = setSpawnLocationView.addListener(listener)
    override fun setSetWelcomeMessageListener(listener: TextListener) = setWelcomeMessageView.addTextChangedListener(listener)
    override fun setSetFirstKitListener(listener: Listener) = setFirstTimeKitView.addListener(listener)
    override fun setRespawnLocationListListener(listener: Listener) = setRespawnLocationOrderView.addListener(listener)
    override fun setSpawnOnJoinListener(listener: Listener) = setSpawnOnJoinView.addListener(listener)
    override fun setJoinMessageListener(listener: TextListener) = joinServerView.addTextChangedListener(listener)
    override fun setQuitMessageListener(listener: TextListener) = quitServerView.addTextChangedListener(listener)
    override fun setCooldownListener(listener: TextListener) = cooldownView.addTextChangedListener(listener)
    override fun setDelayListener(listener: TextListener) = delayView.addTextChangedListener(listener)

    override fun setConfirmLocationListener(listener: Listener) {
        confirmLocationButton?.addListener(listener)
    }

    override fun setTryAgainListener(listener: Listener) {
        tryAgainButton?.addListener(listener)
    }

    override fun setCancelListener(listener: Listener) {
        cancelButton?.addListener(listener)
    }

    override fun setCaptureSpawnListener(listener: Listener) {
        captureButton?.addListener(listener)
    }

    override fun setSelectKitListener(listener: Listener) {
        kitSelectButton?.addListener(listener)
    }

    override fun setKitTitle(title: String) {
        kitTitleView?.update(text = title)
    }

    override fun setKitAdapter(adapter: PagerViewAdapter) {
        kitsPager?.attachAdapter(adapter)
    }

    override fun setPagerListener(listener: PageListener) {
        kitsPager?.addPagerListener(listener)
    }

    override fun setRespawnCallbacks(
        priorityCallback: (RespawnType, PriorityDirection) -> Unit,
        enableCallback: (RespawnType, Boolean) -> Unit
    ) {
        this.priorityCallback = priorityCallback
        this.enableCallback = enableCallback
    }

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.SPAWN_CONFIG_TITLE.resource()),
            size = 16,
        )

        contentView = addViewContainer(
            modifier = Modifier()
                .size(850, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 100, bottom = 100),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

    override fun setRespawnListState(respawn: List<RespawnType>, finishedCallback: Listener) {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .centerHorizontally()
                        .margins(top = 200),
                    text = R.getString(player, S.CONFIG_UPDATE_RESPAWN_ORDER_TITLE.resource()),
                    size = 14,
                )

                val directions = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .centerHorizontally(),
                    text = R.getString(player, S.CONFIG_UPDATE_RESPAWN_ORDER_MESSAGE.resource()),
                    lineWidth = 400,
                    size = 6,
                )

                val disabledRespawns = RespawnType.entries.filter { !respawn.contains(it) }

                val list = addListFeedView(
                    modifier = Modifier()
                        .size(400, 300)
                        .alignTopToBottomOf(directions)
                        .centerHorizontally()
                        .margins(top = 50),

                    )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(list)
                        .centerHorizontally()
                        .margins(top = 100),
                    text = R.getString(player, S.FINISH_BUTTON.resource()),
                    highlightedText = R.getString(player, S.FINISH_BUTTON.resource()).bolden(),
                    callback = finishedCallback,
                )
            }
        })
    }

    override fun setKitState() {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .centerHorizontally()
                        .margins(top = 200),
                    text = R.getString(player, S.CONFIG_UPDATE_SPAWN_KIT_TITLE.resource()),
                    size = 14,
                )

                val directions = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .centerHorizontally(),
                    text = R.getString(player, S.CONFIG_UPDATE_SPAWN_KIT_MESSAGE.resource()),
                    lineWidth = 400,
                    size = 6,
                )

                kitsPager = addPagerView(
                    modifier = Modifier()
                        .size(800, 200)
                        .alignTopToBottomOf(directions)
                        .centerHorizontally()
                        .margins(top = 50),
                    background = Color.fromARGB(0, 0, 0, 0)
                )

                val pager = kitsPager ?: return

                kitTitleView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(pager)
                        .centerHorizontally()
                        .margins(top = 30),
                    text = R.getString(player, S.KIT_NAME_PLACEHOLDER.resource()),
                    size = 12,
                )

                val kitTitle = kitTitleView ?: return

                kitSelectButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(kitTitle)
                        .centerHorizontally()
                        .margins(top = 50),
                    text = R.getString(player, S.SELECT_KIT_BUTTON.resource()),
                    highlightedText = R.getString(player, S.SELECT_KIT_BUTTON.resource()).bolden(),
                )
            }
        })
    }

    override fun setLocationDirectionsState(newSpawn: Location?) {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .centerHorizontally()
                        .margins(top = 200),
                    text = R.getString(player, S.CONFIG_UPDATE_SPAWN_POSITION_TITLE.resource()),
                    size = 14,
                )

                if (newSpawn == null) {
                    val directions = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(title)
                            .centerHorizontally(),
                        text = R.getString(player, S.CONFIG_UPDATE_SPAWN_POSITION_MESSAGE.resource()),
                        lineWidth = 400,
                        size = 6,
                    )

                    captureButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(directions)
                            .centerHorizontally()
                            .margins(top = 100),
                        text = R.getString(player, S.CAPTURE_BUTTON.resource()),
                        highlightedText = R.getString(player, S.CAPTURE_BUTTON.resource()).bolden(),
                    )
                } else {

                    val directions = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(title)
                            .centerHorizontally(),
                        text = R.getString(player, S.CONFIG_UPDATE_SPAWN_POSITION_CAPTURE_MESSAGE.resource()),
                        lineWidth = 400,
                        size = 6,
                    )

                    val location = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(directions)
                            .centerHorizontally()
                            .margins(top = 50),
                        size = 8,
                        text = R.getString(player, S.CONFIG_SPAWN_POSITION.resource(), "%.2f".format(newSpawn.x), "%.2f".format(newSpawn.y), "%.2f".format(newSpawn.z), "%.2f".format(newSpawn.yaw), "%.2f".format(newSpawn.pitch)),
                    )

                    confirmLocationButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(location)
                            .centerHorizontally()
                            .margins(top = 100),
                        text = R.getString(player, S.CONFIRM_BUTTON.resource()),
                        highlightedText = R.getString(player, S.CONFIRM_BUTTON.resource()).bolden(),
                    )

                    tryAgainButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .x(-500)
                            .alignTopToBottomOf(location)
                            .margins(top = 100),
                        text = R.getString(player, S.TRY_AGAIN_BUTTON.resource()),
                        highlightedText = R.getString(player, S.TRY_AGAIN_BUTTON.resource()).bolden(),
                    )

                    cancelButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .x(500)
                            .alignTopToBottomOf(location)
                            .margins(top = 100),
                        text = R.getString(player, S.CANCEL_BUTTON.resource()),
                        highlightedText = R.getString(player, S.CANCEL_BUTTON.resource()).bolden(),
                    )
                }
            }
        })
    }

    override fun setSettingsState() {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                addListFeedView(
                    modifier = Modifier()
                        .size(MATCH_PARENT, MATCH_PARENT)
                        .center(),
                    background = Color.fromARGB(0, 0, 0, 0),
                    content = object : ContextListener<ViewContainer>() {
                        override fun ViewContainer.invoke() {
                            val enableSpawnTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToTopOf(this)
                                    .alignStartToStartOf(this),
                                size = 6,
                                text = R.getString(player, S.CONFIG_ENABLE_SPAWN_TITLE.resource()),
                            )

                            val enableSpawnMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(enableSpawnTitle)
                                    .alignStartToStartOf(enableSpawnTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_ENABLE_SPAWN_MESSAGE.resource()),
                            )

                            enableSpawnButton = addButtonView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(enableSpawnTitle)
                                    .alignBottomToTopOf(enableSpawnMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_ENABLE_SPAWN_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_ENABLE_SPAWN_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val setSpawnTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(enableSpawnMessage)
                                    .alignStartToStartOf(enableSpawnMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SET_SPAWN_LOCATION_TITLE.resource()),
                            )

                            val setSpawnMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setSpawnTitle)
                                    .alignStartToStartOf(setSpawnTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_SET_SPAWN_LOCATION_MESSAGE.resource()),
                            )

                            setSpawnLocationView = addButtonView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(setSpawnTitle)
                                    .alignBottomToTopOf(setSpawnMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SET_SPAWN_LOCATION_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_SET_SPAWN_LOCATION_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val setWelcomeMessageTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setSpawnMessage)
                                    .alignStartToStartOf(setSpawnMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SET_WELCOME_MESSAGE_TITLE.resource()),
                            )

                            val setWelcomeMessageMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setWelcomeMessageTitle)
                                    .alignStartToStartOf(setWelcomeMessageTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_SET_WELCOME_MESSAGE_MESSAGE.resource()),
                            )

                            setWelcomeMessageView = addTextInputView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(setWelcomeMessageTitle)
                                    .alignBottomToTopOf(setWelcomeMessageMessage),
                                size = 6,
                                alignment = Alignment.LEFT,
                                text = R.getString(player, S.CONFIG_SET_WELCOME_MESSAGE_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_SET_WELCOME_MESSAGE_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val setFirstTimeKitTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setWelcomeMessageMessage)
                                    .alignStartToStartOf(setWelcomeMessageMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SET_KIT_TITLE.resource()),
                            )

                            val setFirstTimeKitMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setFirstTimeKitTitle)
                                    .alignStartToStartOf(setFirstTimeKitTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_SET_KIT_MESSAGE.resource()),
                            )

                            setFirstTimeKitView = addButtonView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(setFirstTimeKitTitle)
                                    .alignBottomToTopOf(setFirstTimeKitMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SET_KIT_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_SET_KIT_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val respawnLocationTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(setFirstTimeKitMessage)
                                    .alignStartToStartOf(setFirstTimeKitMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_RESPAWN_ORDER_TITLE.resource()),
                            )

                            val respawnLocationMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(respawnLocationTitle)
                                    .alignStartToStartOf(respawnLocationTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_RESPAWN_ORDER_MESSAGE.resource()),
                            )

                            setRespawnLocationOrderView = addButtonView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(respawnLocationTitle)
                                    .alignBottomToTopOf(respawnLocationMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_RESPAWN_ORDER_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_RESPAWN_ORDER_DEFAULT_VALUE.resource()),
                            )

                            val spawnOnJoinTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(respawnLocationMessage)
                                    .alignStartToStartOf(respawnLocationMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SPAWN_ON_JOIN_TITLE.resource()),
                            )

                            val spawnOnJoinMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(spawnOnJoinTitle)
                                    .alignStartToStartOf(spawnOnJoinTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_SPAWN_ON_JOIN_MESSAGE.resource()),
                            )

                            setSpawnOnJoinView = addButtonView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(spawnOnJoinTitle)
                                    .alignBottomToTopOf(spawnOnJoinMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_SPAWN_ON_JOIN_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_SPAWN_ON_JOIN_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val joinServerMessageTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(spawnOnJoinMessage)
                                    .alignStartToStartOf(spawnOnJoinMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_PLAYER_JOIN_TITLE.resource()),
                            )

                            val joinServerMessageMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(joinServerMessageTitle)
                                    .alignStartToStartOf(joinServerMessageTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_PLAYER_JOIN_MESSAGE.resource()),
                            )

                            joinServerView = addTextInputView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(joinServerMessageTitle)
                                    .alignBottomToTopOf(joinServerMessageMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_PLAYER_JOIN_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_PLAYER_JOIN_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val quitServerMessageTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(joinServerMessageMessage)
                                    .alignStartToStartOf(joinServerMessageMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_PLAYER_LEFT_TITLE.resource()),
                            )

                            val quitServerMessageMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(quitServerMessageTitle)
                                    .alignStartToStartOf(quitServerMessageTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_PLAYER_LEFT_MESSAGE.resource()),
                            )

                            quitServerView = addTextInputView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(quitServerMessageTitle)
                                    .alignBottomToTopOf(quitServerMessageMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_PLAYER_LEFT_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_PLAYER_LEFT_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val cooldownTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(quitServerMessageMessage)
                                    .alignStartToStartOf(quitServerMessageMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_COOLDOWN_TITLE.resource()),
                            )

                            val cooldownMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(cooldownTitle)
                                    .alignStartToStartOf(cooldownTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_COOLDOWN_MESSAGE.resource()),
                            )

                            cooldownView = addTextInputView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(cooldownTitle)
                                    .alignBottomToTopOf(cooldownMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()).bolden(),
                            )

                            val delayTitle = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(cooldownMessage)
                                    .alignStartToStartOf(cooldownMessage)
                                    .margins(top = 100),
                                size = 6,
                                text = R.getString(player, S.CONFIG_DELAY_TITLE.resource()),
                            )

                            val delayMessage = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(delayTitle)
                                    .alignStartToStartOf(delayTitle),
                                alignment = Alignment.LEFT,
                                lineWidth = 300,
                                size = 4,
                                text = R.getString(player, S.CONFIG_DELAY_MESSAGE.resource()),
                            )

                            delayView = addTextInputView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .position(600, 0)
                                    .alignTopToBottomOf(delayTitle)
                                    .alignBottomToTopOf(delayMessage),
                                size = 6,
                                text = R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()),
                                highlightedText = R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()).bolden(),
                            )


                            messageView = addTextView(
                                modifier = Modifier()
                                    .size(WRAP_CONTENT, WRAP_CONTENT)
                                    .alignTopToBottomOf(spawnOnJoinMessage)
                                    .centerHorizontally()
                                    .margins(top = 200),
                                size = 4,
                                text = ""
                            )
                        }
                    }
                )
            }
        })
    }

    override fun setEnabledText(text: String) {
        enableSpawnButton.text = "${ChatColor.GOLD}$text"
        enableSpawnButton.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun setJoinMessageText(text: String) {
        joinServerView.text = "${ChatColor.GOLD}$text"
        joinServerView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun setQuitMessageText(text: String) {
        quitServerView.text = "${ChatColor.GOLD}$text"
        quitServerView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun setCooldownText(text: String) {
        cooldownView.text = "${ChatColor.GOLD}$text"
        cooldownView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun setDelayText(text: String) {
        delayView.text = "${ChatColor.GOLD}$text"
        delayView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun updateSetSpawnText(text: String) {
        setSpawnLocationView.text = "${ChatColor.GOLD}$text"
        setSpawnLocationView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun updateSetWelcomeMessageText(text: String) {
        setWelcomeMessageView.text = "${ChatColor.GOLD}$text"
        setWelcomeMessageView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun updateSetFirstKitText(text: String) {
        setFirstTimeKitView.text = "${ChatColor.GOLD}$text"
        setFirstTimeKitView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun updateRespawnLocationListText(text: String) {
        setRespawnLocationOrderView.text = "${ChatColor.GOLD}$text"
        setRespawnLocationOrderView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun updateSpawnOnJoinText(text: String) {
        setSpawnOnJoinView.text = "${ChatColor.GOLD}$text"
        setSpawnOnJoinView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${text.bolden()}"
    }

    override fun setMessage(message: String) {
        messageView.update(text = message)
    }
}

interface SpawnConfigPresenter: Presenter {
    fun updateSetSpawnText(text: String)
    fun updateSetWelcomeMessageText(text: String)
    fun updateSetFirstKitText(text: String)
    fun updateRespawnLocationListText(text: String)
    fun updateSpawnOnJoinText(text: String)
    fun setJoinMessageText(text: String)
    fun setQuitMessageText(text: String)
    fun setCooldownText(text: String)
    fun setDelayText(text: String)
    fun setEnabledText(text: String)
    fun setEnableSpawnListener(listener: Listener)
    fun setSpawnLocationListener(listener: Listener)
    fun setSetWelcomeMessageListener(listener: TextListener)
    fun setSetFirstKitListener(listener: Listener)
    fun setRespawnLocationListListener(listener: Listener)
    fun setSpawnOnJoinListener(listener: Listener)
    fun setJoinMessageListener(listener: TextListener)
    fun setQuitMessageListener(listener: TextListener)
    fun setDelayListener(listener: TextListener)
    fun setCooldownListener(listener: TextListener)
    fun setMessage(message: String)

    fun setSettingsState()
    fun setLocationDirectionsState(newSpawn: Location? = null)
    fun setCaptureSpawnListener(listener: Listener)
    fun setConfirmLocationListener(listener: Listener)
    fun setTryAgainListener(listener: Listener)
    fun setCancelListener(listener: Listener)
    fun setKitState()
    fun setRespawnListState(respawn: List<RespawnType>, finishedCallback: Listener)
    fun setRespawnCallbacks(priorityCallback: (RespawnType, PriorityDirection) -> Unit, enableCallback: (RespawnType, Boolean) -> Unit)
    fun setKitAdapter(adapter: PagerViewAdapter)
    fun setPagerListener(listener: PageListener)
    fun setKitTitle(title: String)
    fun setSelectKitListener(listener: Listener)
}

class SpawnConfigInteractor(
    private val player: Player,
    private val presenter: SpawnConfigPresenter,
    private val spawnRepository: SpawnRepository,
    private val kitRepository: KitRepository,
): Interactor(presenter) {

    private var state = SpawnConfigState.SETTINGS
    private var newSpawn: Location? = null

    override fun onCreate() {
        super.onCreate()
        setBlockState()
    }

    override fun onResume(newOrigin: Location?) {
        if (state == SpawnConfigState.CAPTURE) return

        super.onResume(newOrigin)
        setBlockState()
    }

    private fun setBlockState() {
        when(state) {
            SpawnConfigState.LOCATION -> setLocationState()
            else -> setSettingsState()
        }
    }

    private fun setLocationState() {
        presenter.setLocationDirectionsState(newSpawn)
        presenter.setCaptureSpawnListener(object : Listener {
            override fun invoke() {
                state = SpawnConfigState.LOCATION
                countdown()
            }
        })

        presenter.setTryAgainListener(object : Listener {
            override fun invoke() {
                state = SpawnConfigState.LOCATION
                countdown()
            }
        })

        presenter.setCancelListener(object : Listener {
            override fun invoke() {
                state = SpawnConfigState.SETTINGS
                newSpawn = null
                setSettingsState()
            }
        })

        presenter.setConfirmLocationListener(object : Listener {
            override fun invoke() {
                val newSpawn = newSpawn ?: return
                spawnRepository.setSpawn(newSpawn)
                state = SpawnConfigState.SETTINGS
                this@SpawnConfigInteractor.newSpawn = null
                setSettingsState()
            }
        })
    }

    private fun setSettingsState() {
        newSpawn = null
        presenter.setSettingsState()

        presenter.setEnabledText(spawnRepository.model.enabled.toString().titlecase())
        presenter.updateSetWelcomeMessageText(spawnRepository.model.welcomeMessage)
        presenter.updateSpawnOnJoinText(spawnRepository.model.spawnOnJoin.toString().titlecase())
        presenter.setJoinMessageText(spawnRepository.model.joinMessage)
        presenter.setQuitMessageText(spawnRepository.model.quitMessage)
        presenter.setCooldownText(R.getString(player, S.CONFIG_TELEPORT_INPUT.resource(), spawnRepository.model.cooldown, if (spawnRepository.model.cooldown != 1) R.getString(player, S.PLURAL.resource()) else ""))
        presenter.setDelayText(R.getString(player, S.CONFIG_TELEPORT_INPUT.resource(), spawnRepository.model.delay, if (spawnRepository.model.delay != 1) R.getString(player, S.PLURAL.resource()) else ""))

        val respawnOrderList = StringBuilder()
        spawnRepository.model.respawnLocation.forEach {
            respawnOrderList.append("${it.title}\n")
        }

        presenter.updateRespawnLocationListText(respawnOrderList.toString().trim())

        spawnRepository.model.spawnLocation?.let {
            val locationString = "${ChatColor.BOLD}${Bukkit.getServer().getWorld(it.worldUUID)?.name ?: ""}\n${ChatColor.GOLD}${"%.2f".format(it.x)} ${"%.2f".format(it.y)} ${"%.2f".format(it.z)}"
            presenter.updateSetSpawnText(locationString)
        }

        spawnRepository.model.spawnKit.let { spawnKitUuid ->
            val kit = kitRepository.getKits().find { it.uuid == spawnKitUuid } ?: return@let
            presenter.updateSetFirstKitText(kit.name)
        }

        presenter.setEnableSpawnListener(object : Listener {
            override fun invoke() {
                val isEnabled = !spawnRepository.model.enabled
                spawnRepository.setEnabled(isEnabled)
                presenter.setEnabledText(isEnabled.toString().titlecase())
            }
        })

        presenter.setJoinMessageListener(object : TextListener {
            override fun invoke(text: String) {
                val newJoinMessage = text.colorize()
                spawnRepository.setPlayerJoinMessage(newJoinMessage)
                presenter.setJoinMessageText(newJoinMessage)
            }
        })

        presenter.setQuitMessageListener(object : TextListener {
            override fun invoke(text: String) {
                val newQuitMessage = text.colorize()
                spawnRepository.setPlayerQuitMessage(newQuitMessage)
                presenter.setQuitMessageText(newQuitMessage)
            }
        })

        presenter.setCooldownListener(object : TextListener {
            override fun invoke(text: String) {
                val cooldown = text.toIntOrNull()
                if (cooldown == null) {
                    presenter.setMessage(R.getString(player, S.CONFIG_COOLDOWN_ERROR_MESSAGE.resource()))
                    presenter.setCooldownText(R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()))
                    return
                }


                spawnRepository.setCooldown(cooldown)
            }
        })

        presenter.setDelayListener(object : TextListener {
            override fun invoke(text: String) {
                val delay = text.toIntOrNull()
                if (delay == null) {
                    presenter.setMessage(R.getString(player, S.CONFIG_DELAY_ERROR_MESSAGE.resource()))
                    presenter.setDelayText(R.getString(player, S.CONFIG_TELEPORT_DEFAULT_VALUE.resource()))
                    return
                }

                spawnRepository.setDelay(delay)
            }
        })

        presenter.setSpawnLocationListener(object : Listener {
            override fun invoke() {
                setLocationState()
            }
        })

        presenter.setSetWelcomeMessageListener(object : TextListener {
            override fun invoke(text: String) {
                val newMessage = text.colorize()
                presenter.updateSetWelcomeMessageText(newMessage)
                spawnRepository.setWelcomeMessage(newMessage)
            }
        })

        presenter.setSetFirstKitListener(object : Listener {
            override fun invoke() {
                presenter.setKitState()
                presenter.setKitAdapter(SpawnKitPagerAdapter(kitRepository))
                var selectedKit = kitRepository.getKits().firstOrNull() ?: return

                presenter.setKitTitle(selectedKit.name)

                presenter.setPagerListener(object : PageListener {
                    override fun invoke(page: Int) {
                        selectedKit = kitRepository.getKits()[page]
                        presenter.setKitTitle(selectedKit.name)
                    }
                })

                presenter.setSelectKitListener(object : Listener {
                    override fun invoke() {
                        spawnRepository.setSpawnKit(selectedKit)
                        setSettingsState()
                    }
                })
            }
        })

        presenter.setRespawnLocationListListener(object : Listener {
            override fun invoke() {
                presenter.setRespawnListState(spawnRepository.model.respawnLocation, object : Listener {
                    override fun invoke() {
                        setSettingsState()
                    }
                })
            }
        })

        presenter.setSpawnOnJoinListener(object : Listener {
            override fun invoke() {
                val newValue = !spawnRepository.model.spawnOnJoin
                presenter.updateSpawnOnJoinText(newValue.toString().titlecase())
                spawnRepository.setSpawnOnJoin(newValue)
            }
        })

        presenter.setRespawnCallbacks(
            priorityCallback = { respawn, priority ->
                if (spawnRepository.updateRespawnPriority(respawn, priority)) {
                    presenter.setRespawnListState(spawnRepository.model.respawnLocation, object : Listener {
                        override fun invoke() {
                            setSettingsState()
                        }
                    })
                }
            },
            enableCallback = { respawn, enabled ->
                if (enabled) {
                    spawnRepository.addRespawnLocation(respawn)
                } else {
                    spawnRepository.removeRespawnLocation(respawn)
                }

                presenter.setRespawnListState(spawnRepository.model.respawnLocation, object : Listener {
                    override fun invoke() {
                        setSettingsState()
                    }
                })
            }
        )
    }

    private fun countdown() {
        minimize()
        state = SpawnConfigState.CAPTURE
        val countdownJob = CoroutineScope(Dispatchers.IO).launch {
            var countdown = 3

            while (countdown > 0) {
                CoroutineScope(DudeDispatcher()).launch {
                    player.sendTitle("${ChatColor.GREEN}$countdown", null, 0, 10, 8)
                }
                delay(1.seconds)
                countdown--
            }
        }

        countdownJob.invokeOnCompletion {
            CoroutineScope(DudeDispatcher()).launch {
                newSpawn = player.location.clone()
                state = SpawnConfigState.LOCATION
                maximize()
            }
        }

        countdownJob.disposeOn(disposer = this)
    }
}

class SpawnKitPagerAdapter(private val kitRepository: KitRepository): PagerViewAdapter() {
    override fun getCount(): Int = kitRepository.getKits().size

    override fun renderElement(selected: Boolean, index: Int, parent: ViewContainer) {
        val kits = kitRepository.getKits()
        if (kits.isEmpty()) return

        parent.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                addItemView(
                    modifier = Modifier()
                        .size(150, 150)
                        .center(),
                    item = Material.valueOf(kits[index].icon)
                )
            }
        })
    }

}

enum class PriorityDirection {
    UP,
    DOWN,
}

enum class SpawnConfigState {
    SETTINGS,
    LOCATION,
    CAPTURE,
}
