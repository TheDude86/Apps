package com.mcmlr.system.products.landing

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
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
    origin: Location,
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
    origin: Location,
    private val spawnRepository: SpawnRepository,
    private val permissionsRepository: PermissionsRepository,
): ViewController(player, origin), SpawnShortcutPresenter {

    private var spawn: ButtonView? = null
    private var back: ButtonView? = null

    override fun setSpawnListener(listener: () -> Unit) {
        spawn?.addListener(listener)
    }

    override fun setBackListener(listener: () -> Unit) {
        back?.addListener(listener)
    }

    override fun createView() {

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${if (spawnRepository.model.enabled) "Spawn" else "Back"}",
            size = 6,
        )

        val favorites = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(this)
                .margins(top = 50, start = 50),
            text = "${ChatColor.GRAY}${ChatColor.BOLD}Teleports",
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
        ) {
            if (spawnRepository.model.enabled) {
                spawn = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this),
                    text = "${ChatColor.GOLD}Spawn",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Spawn",
                    size = 8,
                )
            }

            if (permissionsRepository.checkPermission(player, PermissionNode.BACK)) {
                back = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this),
                    text = "${ChatColor.GOLD}Back",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                    size = 8,
                )
            }
        }

    }
}

interface SpawnShortcutPresenter: Presenter {
    fun setSpawnListener(listener: () -> Unit)
    fun setBackListener(listener: () -> Unit)
}

class SpawnShortcutInteractor(
    private val player: Player,
    private val presenter: SpawnShortcutPresenter,
    private val spawnRepository: SpawnRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setSpawnListener {
            val spawn = spawnRepository.model.spawnLocation?.toLocation() ?: return@setSpawnListener
            player.teleport(spawn)
            close()
        }

        presenter.setBackListener {
            val back = playerTeleportRepository.model.backLocation?.location?.toLocation() ?: return@setBackListener
            player.teleport(back)
            close()
        }
    }
}
