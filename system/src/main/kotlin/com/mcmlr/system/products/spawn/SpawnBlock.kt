package com.mcmlr.system.products.spawn

import com.mcmlr.system.products.teleport.PlayerTeleportRepository
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class SpawnBlock @Inject constructor(
    player: Player,
    origin: Location,
    spawnRepository: SpawnRepository,
    teleportRepository: PlayerTeleportRepository,
): Block(player, origin) {
    private val view = SpawnViewController(player, origin, spawnRepository)
    private val interactor = SpawnInteractor(player, view, spawnRepository, teleportRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SpawnViewController(
    private val player: Player,
    origin: Location,
    private val spawnRepository: SpawnRepository,
): NavigationViewController(player, origin), SpawnPresenter {

    private lateinit var teleportBackButton: ButtonView

    private var spawnButton: ButtonView? = null

    override fun setSpawnListener(listener: Listener) {
        spawnButton?.addListener(listener)
    }

    override fun setBackListener(listener: Listener) = teleportBackButton.addListener(listener)

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Spawn",
            size = 16,
        )

        if (spawnRepository.model.enabled) {
            spawnButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .x(-400)
                    .centerVertically(),
                size = 20,
                text = "${ChatColor.GOLD}Spawn",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Spawn",
            )

            teleportBackButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .x(400)
                    .centerVertically(),
                size = 20,
                text = "${ChatColor.GOLD}Back",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
            )
        } else {
            teleportBackButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .center(),
                size = 20,
                text = "${ChatColor.GOLD}Back",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
            )
        }

    }

}

interface SpawnPresenter: Presenter {
    fun setSpawnListener(listener: Listener)
    fun setBackListener(listener: Listener)
}

class SpawnInteractor(
    private val player: Player,
    private val presenter: SpawnPresenter,
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
