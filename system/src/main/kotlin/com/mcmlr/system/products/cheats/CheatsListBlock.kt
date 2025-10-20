package com.mcmlr.system.products.cheats

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class CheatsListBlock @Inject constructor(
    player: Player,
    origin: Location,
    selectedCheatRepository: SelectedCheatRepository,
): Block(player, origin) {
    private val view = CheatsListViewController(player, origin)
    private val interactor = CheatsListInteractor(view, selectedCheatRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class CheatsListViewController(
    private val player: Player,
    origin: Location,
): ViewController(player, origin), CheatsListPresenter {

    private lateinit var cheatsView: ListFeedView

    override fun setFeed(cheats: List<CheatType>, callback: (CheatType) -> Unit) {
        cheatsView.updateView {
            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .margins(bottom = 50),
                text = "${ChatColor.GRAY}${ChatColor.BOLD}${ChatColor.ITALIC}Cheats",
            )

            cheats.forEach {
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .margins(top = 50, bottom = 50),
                    text = it.title,
                    highlightedText = "${ChatColor.BOLD}${it.title}",
                    callback = object : Listener {
                        override fun invoke() {
                            callback.invoke(it)
                        }
                    }
                )
            }
        }
    }

    override fun createView() {
        cheatsView = addListFeedView(
            modifier = Modifier()
                .size(MATCH_PARENT, MATCH_PARENT)
                .center(),
        )
    }

}

interface CheatsListPresenter: Presenter {
    fun setFeed(cheats: List<CheatType>, callback: (CheatType) -> Unit)
}

class CheatsListInteractor(
    private val presenter: CheatsListPresenter,
    private val selectedCheatRepository: SelectedCheatRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        val cheats = CheatType.entries.toList()

        selectedCheatRepository.setSelectedCheat(cheats.first())

        presenter.setFeed(cheats) {
            selectedCheatRepository.setSelectedCheat(it)
        }
    }
}

enum class CheatType(val title: String, val cta: String, val description: String) {
    SPAWNER("spawner", "Choose Mob", "This cheat will let you select a mob and the next mob spawner you left click will be set to that mob."),
    SUICIDE("suicide", "Die X(", "This cheat allows you to kill yourself. (It's painless)"),
    CONDENSE("condense", "Condense", "This cheat will convert all applicable items in your inventory into block form."),
    BOOK("book", "Open", "This cheat allows you to reopen a closed book in your hand."),
    HAT("hat", "Set Hat", "This cheat allows you to place the item in your hand on your head as a hat."),
    MORE("more", "Fill", "This cheat fills the item stack in your hand to it's maximum amount."),
    REST("rest", "Rest Now", "This cheat will reset your sleepiness.  It is the same as sleeping in a bed, phantoms will no longer "),
    WEATHER("weather", "Choose Weather", "This cheat let's you set the weather you see but will not change the weather for other players."),
    BOTTOM("bottom", "Teleport", "This cheat teleports you to the block at the lowest Y position at your current location."),
    BREAK("break", "Break", "This cheat will immediately break the next block."),
    COMPASS("compass", "Show Location", "This cheat displays your current position, pitch and yaw."),
    WORLD("world", "Select World", "This cheat allows you to select which world to teleport to.  You will be teleported to that world's spawn location."),
    EXTINGUISH("extinguish", "Extinguish", "This cheat puts yourself out if you're on fire."),
    LIGHTNING("lightning", "Smite", "This cheat will shoot a lightning bolt at the block you're looking at the next time you left click."),
    ICE("ice", "CTA", "This cheat will apply the freeze effect without causing damage to you."),
    ITEM("item", "Select Item", "This cheat lets you select an item and set a custom name, lore and enchantments with chat color support."),
    FIREWORK("firework", "Create Fireworks", "This cheat is a tool to create custom fireworks setting it's trail, twinkle, shape, burst color and fade color."),
    ENCHANT("enchant", "CTA", "${ChatColor.RED}Merge with item cheat"),
    TIME("time", "Set Time", "This cheat will set the time you see but will not affect other players."),
    GIVE("give", "CTA", "${ChatColor.RED}Merge with item cheat"),
    FLY("fly", "Toggle", "This cheat toggles Creative fly mode."),
    POTION("potion", "Create ", "This cheat is a tool to create custom potions."),
    GOD("god", "Toggle", "This cheat toggles God mode."),
    CLEAR("clear", "Clear Items", "Use this cheat to select items in your inventory to be cleared."),
    UNLIMITED("unlimited", "Toggle", "This cheat will toggle the ability to place unlimited blocks like in creative mode."),
    ANTIOCH("antioch", "Fire the holy hand grenade!", "${ChatColor.GREEN}${ChatColor.ITALIC}This cheat deploys real block of TNT at the block you're looking at the next time you left click."),
    FIREBALL("fireball", "Shoot", "This cheat will shoot a fireball in the direction you're looking the next time you left click."),
    ITEM_LORE("item lore", "CTA", "${ChatColor.RED}Merge with item cheat"),
    ITEM_NAME("item name", "CTA", "${ChatColor.RED}Merge with item cheat"),
    BURN("burn", "Burn!", "This cheat will set you on fire."),
    FEED("feed", "Eat", "This cheat will fill your hunger bar."),
    HEAL("heal", "Heal", "This cheat will fill your health bar."),
    KITTY_CANNON("kitty cannon", "Kitty!", "This cheat shoots a cat with a fake explosion in the direction you're looking the next time you click."),
    BEEZOOKA("beezooka", "The Bees!", "This cheat shoots a bee with a fake explosion in the direction you're looking the next time you click."),
    NUKE("nuke", "I am become Death...", "This cheat will drop fake 16 TNT blocks on the select players, should give them a good scare!"),
    THUNDER("thunder", "CTA", "${ChatColor.YELLOW}No idea what this does"),
}
