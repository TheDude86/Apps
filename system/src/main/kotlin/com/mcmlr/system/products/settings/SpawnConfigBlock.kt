package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.products.kits.KitRepository
import com.mcmlr.system.products.spawn.RespawnType
import com.mcmlr.system.products.spawn.SpawnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SpawnConfigBlock @Inject constructor(
    player: Player,
    origin: Location,
    spawnRepository: SpawnRepository,
    kitRepository: KitRepository,
) : Block(player, origin) {
    private val view: SpawnConfigViewController = SpawnConfigViewController(player, origin)
    private val interactor: SpawnConfigInteractor = SpawnConfigInteractor(player, view, spawnRepository, kitRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class SpawnConfigViewController(player: Player, origin: Location): NavigationViewController(player, origin),
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

    override fun setEnableSpawnListener(listener: () -> Unit) = enableSpawnButton.addListener(listener)
    override fun setSpawnLocationListener(listener: () -> Unit) = setSpawnLocationView.addListener(listener)
    override fun setSetWelcomeMessageListener(listener: (String) -> Unit) = setWelcomeMessageView.addTextChangedListener(listener)
    override fun setSetFirstKitListener(listener: () -> Unit) = setFirstTimeKitView.addListener(listener)
    override fun setRespawnLocationListListener(listener: () -> Unit) = setRespawnLocationOrderView.addListener(listener)
    override fun setSpawnOnJoinListener(listener: () -> Unit) = setSpawnOnJoinView.addListener(listener)
    override fun setJoinMessageListener(listener: (String) -> Unit) = joinServerView.addTextChangedListener(listener)
    override fun setQuitMessageListener(listener: (String) -> Unit) = quitServerView.addTextChangedListener(listener)
    override fun setCooldownListener(listener: (String) -> Unit) = cooldownView.addTextChangedListener(listener)
    override fun setDelayListener(listener: (String) -> Unit) = delayView.addTextChangedListener(listener)

    override fun setConfirmLocationListener(listener: () -> Unit) {
        confirmLocationButton?.addListener(listener)
    }

    override fun setTryAgainListener(listener: () -> Unit) {
        tryAgainButton?.addListener(listener)
    }

    override fun setCancelListener(listener: () -> Unit) {
        cancelButton?.addListener(listener)
    }

    override fun setCaptureSpawnListener(listener: () -> Unit) {
        captureButton?.addListener(listener)
    }

    override fun setSelectKitListener(listener: () -> Unit) {
        kitSelectButton?.addListener(listener)
    }

    override fun setKitTitle(title: String) {
        kitTitleView?.setTextView(title)
    }

    override fun setKitAdapter(adapter: PagerViewAdapter) {
        kitsPager?.attachAdapter(adapter)
    }

    override fun setPagerListener(listener: (Int) -> Unit) {
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Spawn Settings",
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

    override fun setRespawnListState(respawn: List<RespawnType>, finishedCallback: () -> Unit) {
        contentView.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 200),
                text = "${ChatColor.BOLD}Set spawn kit",
                size = 14,
            )

            val directions = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .centerHorizontally(),
                text = "${ChatColor.GRAY}Select the order of locations to send players after they die and respawn.  Disabling a location will ignore that location when finding a respawn location and if a player does not have a location set, like a home for example, then that location will be skipped as well.",
                lineWidth = 400,
                size = 6,
            )

            val disabledRespawns = RespawnType.entries.filter { !respawn.contains(it) }

            val list = addListFeedView(
                modifier = Modifier()
                    .size(400, 300)
                    .alignTopToBottomOf(directions)
                    .centerHorizontally()
                    .margins(top = 50)
            ) {
                respawn.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 30),
                        background = Color.fromARGB(0, 0, 0, 0)
                    ) {
                        val location = addTextView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .center(),
                            text = it.title,
                            size = 6
                        )

                        val arrowUp = addItemButtonView(
                            modifier = Modifier()
                                .size(40, 40)
                                .alignStartToStartOf(this)
                                .alignTopToTopOf(location)
                                .alignBottomToBottomOf(location)
                                .margins(start = 50),
                            item = getPlayerHead("http://textures.minecraft.net/texture/365fc0426230a2e88df29d2d8ec4512e6dbdbc0777b4b83cdda2ede81864d6"),
                        ) {
                            priorityCallback.invoke(it, PriorityDirection.UP)
                        }

                        addItemButtonView(
                            modifier = Modifier()
                                .size(40, 40)
                                .alignStartToEndOf(arrowUp)
                                .alignTopToTopOf(arrowUp)
                                .alignBottomToBottomOf(arrowUp),
                            item = getPlayerHead("http://textures.minecraft.net/texture/4e8ba7863b15a5e40fa7da9629bb866aa22699553e931df1f693cbb1c9f3b6"),
                        ) {
                            priorityCallback.invoke(it, PriorityDirection.DOWN)
                        }

                        addItemButtonView(
                            modifier = Modifier()
                                .size(20, 20)
                                .alignStartToEndOf(location)
                                .alignTopToTopOf(location)
                                .alignBottomToBottomOf(location)
                                .margins(start = 30),
                            item = ItemStack(Material.BARRIER),
                        ) {
                            enableCallback.invoke(it, false)
                        }
                    }
                }

                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 30),
                    background = Color.fromARGB(0, 0, 0, 0)
                )

                disabledRespawns.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 30),
                        background = Color.fromARGB(0, 0, 0, 0)
                    ) {
                        val location = addTextView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .center(),
                            text = "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${it.title}",
                            size = 6
                        )

                        addItemButtonView(
                            modifier = Modifier()
                                .size(20, 20)
                                .alignStartToEndOf(location)
                                .alignTopToTopOf(location)
                                .alignBottomToBottomOf(location)
                                .margins(start = 30),
                            item = ItemStack(Material.KELP),
                        ) {
                            enableCallback.invoke(it, true)
                        }
                    }
                }
            }

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(list)
                    .centerHorizontally()
                    .margins(top = 100),
                text = "${ChatColor.GOLD}Finish",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Finish",
                callback = finishedCallback,
            )
        }
    }

    override fun setKitState() {
        contentView.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 200),
                text = "${ChatColor.BOLD}Set spawn kit",
                size = 14,
            )

            val directions = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .centerHorizontally(),
                text = "${ChatColor.GRAY}Select a kit from your saved kits.  You can go into the Kits app to create a new kit if none of the existing kits work for you.",
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

            val pager = kitsPager ?: return@updateView

            kitTitleView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(pager)
                    .centerHorizontally()
                    .margins(top = 30),
                text = "Kit Title",
                size = 12,
            )

            val kitTitle = kitTitleView ?: return@updateView

            kitSelectButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitTitle)
                    .centerHorizontally()
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Select Kit",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Select Kit",
            )
        }
    }

    override fun setLocationDirectionsState(newSpawn: Location?) {
        contentView.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 200),
                text = "${ChatColor.BOLD}Set spawn location",
                size = 14,
            )

            if (newSpawn == null) {
                val directions = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .centerHorizontally(),
                    text = "${ChatColor.GRAY}Once you're ready, click the \"Capture\" button and a countdown from 3 will begin.  At the end of the countdown, your location and rotation will be captured as the new spawn point and you will have the options to use that as your new server spawn, try again, or cancel.",
                    lineWidth = 400,
                    size = 6,
                )

                captureButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(directions)
                        .centerHorizontally()
                        .margins(top = 100),
                    text = "${ChatColor.GOLD}Capture",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Capture",
                )
            } else {

                val directions = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .centerHorizontally(),
                    text = "${ChatColor.GRAY}Your location has been captured.  Please click \"Confirm\" to set it as the server's new spawn point or \"Try Again\" to capture a new point.",
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
                    text = "${ChatColor.BOLD}New Spawn Location\n${ChatColor.RESET}X:${"%.2f".format(newSpawn.x)} Y:${"%.2f".format(newSpawn.y)} Z:${"%.2f".format(newSpawn.z)} Yaw:${"%.2f".format(newSpawn.yaw)} Pitch:${"%.2f".format(newSpawn.pitch)}",
                )

                confirmLocationButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(location)
                        .centerHorizontally()
                        .margins(top = 100),
                    text = "${ChatColor.GOLD}Confirm",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Confirm",
                )

                tryAgainButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .x(-500)
                        .alignTopToBottomOf(location)
                        .margins(top = 100),
                    text = "${ChatColor.GOLD}Try Again",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Try Again",
                )

                cancelButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .x(500)
                        .alignTopToBottomOf(location)
                        .margins(top = 100),
                    text = "${ChatColor.RED}Cancel",
                    highlightedText = "${ChatColor.RED}${ChatColor.BOLD}Cancel",
                )
            }
        }
    }

    override fun setSettingsState() {
        contentView.updateView {
            addListFeedView(
                modifier = Modifier()
                    .size(MATCH_PARENT, MATCH_PARENT)
                    .center(),
                background = Color.fromARGB(0, 0, 0, 0),
            ) {
                val enableSpawnTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .alignStartToStartOf(this),
                    size = 6,
                    text = "Enable Spawn",
                )

                val enableSpawnMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(enableSpawnTitle)
                        .alignStartToStartOf(enableSpawnTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}Enable this app to use it's custom spawn logic. Spawn controls where new players spawn for the first time, what kits their given, and a custom welcome message.  Spawn also controls custom existing player join and quit messages, respawn logic and teleporting to spawn and previous locations.",
                )

                enableSpawnButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(enableSpawnTitle)
                        .alignBottomToTopOf(enableSpawnMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}Off",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Off",
                )

                val setSpawnTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(enableSpawnMessage)
                        .alignStartToStartOf(enableSpawnMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Set spawn location",
                )

                val setSpawnMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setSpawnTitle)
                        .alignStartToStartOf(setSpawnTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}New players will join the server for the first time, players who teleport to spawn, and players who die and have no other respawn locations set all will be teleported to this location.",
                )

                setSpawnLocationView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(setSpawnTitle)
                        .alignBottomToTopOf(setSpawnMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}No location set",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}No location set",
                )

                val setWelcomeMessageTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setSpawnMessage)
                        .alignStartToStartOf(setSpawnMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Set welcome message",
                )

                val setWelcomeMessageMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setWelcomeMessageTitle)
                        .alignStartToStartOf(setWelcomeMessageTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}This message will be displayed when a new player joins the server for the first time.  This message supports Placeholder API so you can use placeholders to display the new player's name or any other info.",
                )

                setWelcomeMessageView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(setWelcomeMessageTitle)
                        .alignBottomToTopOf(setWelcomeMessageMessage),
                    size = 6,
                    alignment = Alignment.LEFT,
                    text = "${ChatColor.GOLD}Welcome %player_name% to the server!",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Welcome %player_name% to the server!",
                )

                val setFirstTimeKitTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setWelcomeMessageMessage)
                        .alignStartToStartOf(setWelcomeMessageMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Set new player kit",
                )

                val setFirstTimeKitMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setFirstTimeKitTitle)
                        .alignStartToStartOf(setFirstTimeKitTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}Players who join the server for the first time will be automatically given the selected kit.",
                )

                setFirstTimeKitView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(setFirstTimeKitTitle)
                        .alignBottomToTopOf(setFirstTimeKitMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}None",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}None",
                )

                val respawnLocationTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(setFirstTimeKitMessage)
                        .alignStartToStartOf(setFirstTimeKitMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Respawn location order",
                )

                val respawnLocationMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(respawnLocationTitle)
                        .alignStartToStartOf(respawnLocationTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}Set the order for which respawn locations should a player respawn at when they die.  If a player does not have a location set at the top of the respawn order, the rest of the list will be traversed until a location is found.",
                )

                setRespawnLocationOrderView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(respawnLocationTitle)
                        .alignBottomToTopOf(respawnLocationMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}Bed\nSpawn",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Bed\nSpawn",
                )

                val spawnOnJoinTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(respawnLocationMessage)
                        .alignStartToStartOf(respawnLocationMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Teleport to spawn on join",
                )

                val spawnOnJoinMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(spawnOnJoinTitle)
                        .alignStartToStartOf(spawnOnJoinTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}If set to true, all players will be teleported to spawn every time they join the server.",
                )

                setSpawnOnJoinView = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(spawnOnJoinTitle)
                        .alignBottomToTopOf(spawnOnJoinMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}False",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}False",
                )

                val joinServerMessageTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(spawnOnJoinMessage)
                        .alignStartToStartOf(spawnOnJoinMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Player joined message",
                )

                val joinServerMessageMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(joinServerMessageTitle)
                        .alignStartToStartOf(joinServerMessageTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}This message will be sent to all online players when a player joins the server.  This message also supports Placeholder API.",
                )

                joinServerView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(joinServerMessageTitle)
                        .alignBottomToTopOf(joinServerMessageMessage),
                    size = 6,
                    text = "${ChatColor.DARK_GRAY}[${ChatColor.GREEN}+${ChatColor.DARK_GRAY}]${ChatColor.GRAY}%player_name%",
                    highlightedText = "${ChatColor.DARK_GRAY}[${ChatColor.GREEN}+${ChatColor.DARK_GRAY}]${ChatColor.GRAY}%player_name%".bolden(),
                )

                val quitServerMessageTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(joinServerMessageMessage)
                        .alignStartToStartOf(joinServerMessageMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Player left message",
                )

                val quitServerMessageMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(quitServerMessageTitle)
                        .alignStartToStartOf(quitServerMessageTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}This message will be sent to all online players when a player leaves the server.  This message also supports Placeholder API.",
                )

                quitServerView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(quitServerMessageTitle)
                        .alignBottomToTopOf(quitServerMessageMessage),
                    size = 6,
                    text = "${ChatColor.DARK_GRAY}[${ChatColor.RED}-${ChatColor.DARK_GRAY}]${ChatColor.GRAY}%player_name%",
                    highlightedText = "${ChatColor.DARK_GRAY}[${ChatColor.RED}-${ChatColor.DARK_GRAY}]${ChatColor.GRAY}%player_name%".bolden(),
                )

                val cooldownTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(quitServerMessageMessage)
                        .alignStartToStartOf(quitServerMessageMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Teleport cooldown",
                )

                val cooldownMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(cooldownTitle)
                        .alignStartToStartOf(cooldownTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}The amount of time, in seconds, the player must wait after teleporting to a home before they can teleport again.",
                )

                cooldownView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(cooldownTitle)
                        .alignBottomToTopOf(cooldownMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}0 Seconds",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}0 Seconds",
                )

                val delayTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(cooldownMessage)
                        .alignStartToStartOf(cooldownMessage)
                        .margins(top = 100),
                    size = 6,
                    text = "Teleport delay",
                )

                val delayMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(delayTitle)
                        .alignStartToStartOf(delayTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = "${ChatColor.GRAY}The amount of time, in seconds, the player must wait before being teleported.",
                )

                delayView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(delayTitle)
                        .alignBottomToTopOf(delayMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}0 Seconds",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}0 Seconds",
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
        messageView.setTextView(message)
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
    fun setEnableSpawnListener(listener: () -> Unit)
    fun setSpawnLocationListener(listener: () -> Unit)
    fun setSetWelcomeMessageListener(listener: (String) -> Unit)
    fun setSetFirstKitListener(listener: () -> Unit)
    fun setRespawnLocationListListener(listener: () -> Unit)
    fun setSpawnOnJoinListener(listener: () -> Unit)
    fun setJoinMessageListener(listener: (String) -> Unit)
    fun setQuitMessageListener(listener: (String) -> Unit)
    fun setDelayListener(listener: (String) -> Unit)
    fun setCooldownListener(listener: (String) -> Unit)
    fun setMessage(message: String)

    fun setSettingsState()
    fun setLocationDirectionsState(newSpawn: Location? = null)
    fun setCaptureSpawnListener(listener: () -> Unit)
    fun setConfirmLocationListener(listener: () -> Unit)
    fun setTryAgainListener(listener: () -> Unit)
    fun setCancelListener(listener: () -> Unit)
    fun setKitState()
    fun setRespawnListState(respawn: List<RespawnType>, finishedCallback: () -> Unit)
    fun setRespawnCallbacks(priorityCallback: (RespawnType, PriorityDirection) -> Unit, enableCallback: (RespawnType, Boolean) -> Unit)
    fun setKitAdapter(adapter: PagerViewAdapter)
    fun setPagerListener(listener: (Int) -> Unit)
    fun setKitTitle(title: String)
    fun setSelectKitListener(listener: () -> Unit)
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
        presenter.setCaptureSpawnListener {
            state = SpawnConfigState.LOCATION
            countdown()
        }

        presenter.setTryAgainListener {
            state = SpawnConfigState.LOCATION
            countdown()
        }

        presenter.setCancelListener {
            state = SpawnConfigState.SETTINGS
            newSpawn = null
            setSettingsState()
        }

        presenter.setConfirmLocationListener {
            val newSpawn = newSpawn ?: return@setConfirmLocationListener
            spawnRepository.setSpawn(newSpawn)
            state = SpawnConfigState.SETTINGS
            this.newSpawn = null
            setSettingsState()
        }
    }

    private fun setSettingsState() {
        newSpawn = null
        presenter.setSettingsState()

        presenter.setEnabledText(spawnRepository.model.enabled.toString().titlecase())
        presenter.updateSetWelcomeMessageText(spawnRepository.model.welcomeMessage)
        presenter.updateSpawnOnJoinText(spawnRepository.model.spawnOnJoin.toString().titlecase())
        presenter.setJoinMessageText(spawnRepository.model.joinMessage)
        presenter.setQuitMessageText(spawnRepository.model.quitMessage)
        presenter.setCooldownText("${spawnRepository.model.cooldown} Second${if (spawnRepository.model.cooldown != 1) "s" else ""}")
        presenter.setDelayText("${spawnRepository.model.delay} Second${if (spawnRepository.model.cooldown != 1) "s" else ""}")

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

        presenter.setEnableSpawnListener {
            val isEnabled = !spawnRepository.model.enabled
            spawnRepository.setEnabled(isEnabled)
            presenter.setEnabledText(isEnabled.toString().titlecase())
        }

        presenter.setJoinMessageListener {
            val newJoinMessage = it.colorize()
            spawnRepository.setPlayerJoinMessage(newJoinMessage)
            presenter.setJoinMessageText(newJoinMessage)
        }

        presenter.setQuitMessageListener {
            val newQuitMessage = it.colorize()
            spawnRepository.setPlayerQuitMessage(newQuitMessage)
            presenter.setQuitMessageText(newQuitMessage)
        }

        presenter.setCooldownListener {
            val cooldown = it.toIntOrNull()
            if (cooldown == null) {
                presenter.setMessage("${ChatColor.RED}Teleport cooldown values must be whole numbers!")
                presenter.setCooldownText("0 Seconds")
                return@setCooldownListener
            }

            spawnRepository.setCooldown(cooldown)
        }

        presenter.setDelayListener {
            val delay = it.toIntOrNull()
            if (delay == null) {
                presenter.setMessage("${ChatColor.RED}Teleport delay values must be whole numbers!")
                presenter.setDelayText("0 Seconds")
                return@setDelayListener
            }

            spawnRepository.setDelay(delay)
        }

        presenter.setSpawnLocationListener {
            setLocationState()
        }

        presenter.setSetWelcomeMessageListener {
            val newMessage = it.colorize()
            presenter.updateSetWelcomeMessageText(newMessage)
            spawnRepository.setWelcomeMessage(newMessage)
        }

        presenter.setSetFirstKitListener {
            presenter.setKitState()
            presenter.setKitAdapter(SpawnKitPagerAdapter(kitRepository))
            var selectedKit = kitRepository.getKits().firstOrNull() ?: return@setSetFirstKitListener

            presenter.setKitTitle(selectedKit.name)

            presenter.setPagerListener {
                selectedKit = kitRepository.getKits()[it]
                presenter.setKitTitle(selectedKit.name)
            }

            presenter.setSelectKitListener {
                spawnRepository.setSpawnKit(selectedKit)
                setSettingsState()
            }
        }

        presenter.setRespawnLocationListListener {
            presenter.setRespawnListState(spawnRepository.model.respawnLocation) {
                setSettingsState()
            }
        }

        presenter.setSpawnOnJoinListener {
            val newValue = !spawnRepository.model.spawnOnJoin
            presenter.updateSpawnOnJoinText(newValue.toString().titlecase())
            spawnRepository.setSpawnOnJoin(newValue)
        }

        presenter.setRespawnCallbacks(
            priorityCallback = { respawn, priority ->
                if (spawnRepository.updateRespawnPriority(respawn, priority)) {
                    presenter.setRespawnListState(spawnRepository.model.respawnLocation) {
                        setSettingsState()
                    }
                }
            },
            enableCallback = { respawn, enabled ->
                if (enabled) {
                    spawnRepository.addRespawnLocation(respawn)
                } else {
                    spawnRepository.removeRespawnLocation(respawn)
                }

                presenter.setRespawnListState(spawnRepository.model.respawnLocation) {
                    setSettingsState()
                }
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
                kotlinx.coroutines.delay(1.seconds)
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

        parent.updateView {
            addItemView(
                modifier = Modifier()
                    .size(150, 150)
                    .center(),
                item = Material.valueOf(kits[index].icon)
            )
        }
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
