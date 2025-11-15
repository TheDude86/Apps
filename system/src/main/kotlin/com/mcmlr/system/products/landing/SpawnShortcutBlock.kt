package com.mcmlr.system.products.landing

import com.mcmlr.blocks.api.app.R
import com.mcmlr.system.S
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.products.data.PermissionNode
import com.mcmlr.system.products.data.PermissionsRepository
import com.mcmlr.system.products.teleport.PlayerTeleportRepository
import com.mcmlr.system.products.spawn.SpawnRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class SpawnShortcutBlock @Inject constructor(
    player: Player,
    origin: Origin,
    spawnRepository: SpawnRepository,
    playerTeleportRepository: PlayerTeleportRepository,
    permissionsRepository: PermissionsRepository,
): Block(player, origin) {
    private val view = SpawnShortcutViewController(player, origin, spawnRepository, permissionsRepository)
    private val interactor = SpawnShortcutInteractor(player, view, spawnRepository, playerTeleportRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SpawnShortcutViewController(
    private val player: Player,
    origin: Origin,
    private val spawnRepository: SpawnRepository,
    private val permissionsRepository: PermissionsRepository,
): ViewController(player, origin), SpawnShortcutPresenter {

    private var spawn: ButtonView? = null
    private var back: ButtonView? = null

    override fun setSpawnListener(listener: Listener) {
        spawn?.addListener(listener)
    }

    override fun setBackListener(listener: Listener) {
        back?.addListener(listener)
    }

    override fun createView() {

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${if (spawnRepository.model.enabled) R.getString(player, S.SPAWN.resource()) else R.getString(player, S.BACK.resource())}",
            size = 6,
        )

        val favorites = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(this)
                .margins(top = 50, start = 50),
            text = "${ChatColor.GRAY}${ChatColor.BOLD}${R.getString(player, S.TELEPORTS.resource())}",
            size = 5,
        )

        addListView(
            modifier = Modifier()
                .size(300, 200)
                .alignStartToStartOf(favorites)
                .alignEndToEndOf(this)
                .alignTopToBottomOf(favorites)
                .alignBottomToBottomOf(this)
                .margins(top = 20),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    if (spawnRepository.model.enabled) {
                        spawn = addButtonView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignStartToStartOf(this),
                            text = "${ChatColor.GOLD}${R.getString(player, S.SPAWN.resource())}",
                            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.SPAWN.resource())}",
                            size = 8,
                        )
                    }

                    if (permissionsRepository.checkPermission(player, PermissionNode.BACK)) {
                        back = addButtonView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignStartToStartOf(this),
                            text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
                            size = 8,
                        )
                    }
                }
            }
        )

    }
}

interface SpawnShortcutPresenter: Presenter {
    fun setSpawnListener(listener: Listener)
    fun setBackListener(listener: Listener)
}

class SpawnShortcutInteractor(
    private val player: Player,
    private val presenter: SpawnShortcutPresenter,
    private val spawnRepository: SpawnRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setSpawnListener(object : Listener {
            override fun invoke() {
                val spawn = spawnRepository.model.spawnLocation?.toLocation() ?: return
                player.teleport(spawn)
                close()
            }
        })

        presenter.setBackListener(object : Listener {
            override fun invoke() {
                val back = playerTeleportRepository.model.backLocation?.location?.toLocation() ?: return
                player.teleport(back)
                close()
            }
        })
    }
}
