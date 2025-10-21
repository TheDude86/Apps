package com.mcmlr.system.products.cheats.children

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.titlecase
import com.mcmlr.system.products.cheats.ActiveCheatsRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import javax.inject.Inject

class SpawnerBlock @Inject constructor(
    player: Player,
    origin: Location,
    activeCheatsRepository: ActiveCheatsRepository,
): Block(player, origin) {
    private val view = SpawnerViewController(player, origin)
    private val interactor = SpawnerInteractor(player, view, activeCheatsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SpawnerViewController(
    player: Player,
    origin: Location,
): ViewController(player, origin), SpawnerPresenter {

    private lateinit var mobsFeed: ListFeedView

    override fun setMobFeed(mobs: List<EntityType>, callback: (EntityType) -> Unit) {
        mobsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                for (i in 0..mobs.size step 3) {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                for (j in 0..2) {
                                    if (mobs.size > i + j) {
                                        val mob = mobs[i + j]
                                        addButtonView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .x(-600 + (600 * j))
                                                .centerVertically(),
                                            text = "${ChatColor.GOLD}${mob.name.lowercase().replace('_', ' ').titlecase()}",
                                            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${mob.name.lowercase().replace('_', ' ').titlecase()}",
                                            size = 5,
                                            callback = object : Listener {
                                                override fun invoke() {
                                                    callback.invoke(mob)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        })
    }

    override fun createView() {
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.BOLD}Select Mob",
        )

        mobsFeed = addListFeedView(
            modifier = Modifier()
                .size(MATCH_PARENT, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(top = 100)
        )
    }
}


interface SpawnerPresenter: Presenter {
    fun setMobFeed(mobs: List<EntityType>, callback: (EntityType) -> Unit)
}

class SpawnerInteractor(
    private val player: Player,
    private val presenter: SpawnerPresenter,
    private val activeCheatsRepository: ActiveCheatsRepository,
): Interactor(presenter) {

    private val spawnerMobsList = listOf(
        EntityType.ARMADILLO,
        EntityType.ALLAY,
        EntityType.AXOLOTL,
        EntityType.BAT,
        EntityType.BEE,
        EntityType.BLAZE,
        EntityType.BOGGED,
        EntityType.BREEZE,
        EntityType.CAMEL,
        EntityType.CAT,
        EntityType.CAVE_SPIDER,
        EntityType.CHICKEN,
        EntityType.COD,
        EntityType.COW,
        EntityType.CREAKING,
        EntityType.CREEPER,
        EntityType.DOLPHIN,
        EntityType.DONKEY,
        EntityType.DROWNED,
        EntityType.ELDER_GUARDIAN,
        EntityType.END_CRYSTAL,
        EntityType.ENDER_DRAGON,
        EntityType.ENDERMAN,
        EntityType.ENDERMITE,
        EntityType.EVOKER,
        EntityType.FOX,
        EntityType.FROG,
        EntityType.GHAST,
        EntityType.GIANT,
        EntityType.GLOW_SQUID,
        EntityType.GOAT,
        EntityType.GUARDIAN,
        EntityType.HOGLIN,
        EntityType.HORSE,
        EntityType.HUSK,
        EntityType.ILLUSIONER,
        EntityType.IRON_GOLEM,
        EntityType.LLAMA,
        EntityType.MAGMA_CUBE,
        EntityType.MOOSHROOM,
        EntityType.MULE,
        EntityType.OCELOT,
        EntityType.PANDA,
        EntityType.PARROT,
        EntityType.PHANTOM,
        EntityType.PIG,
        EntityType.PIGLIN,
        EntityType.PIGLIN_BRUTE,
        EntityType.PILLAGER,
        EntityType.POLAR_BEAR,
        EntityType.PUFFERFISH,
        EntityType.RABBIT,
        EntityType.RAVAGER,
        EntityType.SHULKER,
        EntityType.SALMON,
        EntityType.SHEEP,
        EntityType.SILVERFISH,
        EntityType.SKELETON,
        EntityType.SKELETON_HORSE,
        EntityType.STRIDER,
        EntityType.SLIME,
        EntityType.SNIFFER,
        EntityType.SNOW_GOLEM,
        EntityType.SPIDER,
        EntityType.SQUID,
        EntityType.STRAY,
        EntityType.TADPOLE,
        EntityType.TRADER_LLAMA,
        EntityType.TROPICAL_FISH,
        EntityType.TURTLE,
        EntityType.VEX,
        EntityType.VILLAGER,
        EntityType.VINDICATOR,
        EntityType.WANDERING_TRADER,
        EntityType.WARDEN,
        EntityType.WITCH,
        EntityType.WITHER_SKELETON,
        EntityType.WITHER,
        EntityType.WOLF,
        EntityType.ZOGLIN,
        EntityType.ZOMBIE,
        EntityType.ZOMBIE_HORSE,
        EntityType.ZOMBIE_VILLAGER,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.EXPERIENCE_ORB,
        EntityType.ARMOR_STAND,
        EntityType.MINECART,
        EntityType.TNT_MINECART,
        EntityType.CHEST_MINECART,
        EntityType.HOPPER_MINECART,
        EntityType.FURNACE_MINECART,
        EntityType.SPAWNER_MINECART,
        EntityType.OAK_BOAT,
        EntityType.SPRUCE_BOAT,
        EntityType.BIRCH_BOAT,
        EntityType.JUNGLE_BOAT,
        EntityType.ACACIA_BOAT,
        EntityType.DARK_OAK_BOAT,
        EntityType.MANGROVE_BOAT,
        EntityType.CHERRY_BOAT,
        EntityType.PALE_OAK_BOAT,
        EntityType.BAMBOO_RAFT,
        EntityType.OAK_CHEST_BOAT,
        EntityType.SPRUCE_CHEST_BOAT,
        EntityType.BIRCH_CHEST_BOAT,
        EntityType.JUNGLE_CHEST_BOAT,
        EntityType.ACACIA_CHEST_BOAT,
        EntityType.DARK_OAK_CHEST_BOAT,
        EntityType.MANGROVE_CHEST_BOAT,
        EntityType.CHERRY_CHEST_BOAT,
        EntityType.PALE_OAK_CHEST_BOAT,
        EntityType.BAMBOO_CHEST_RAFT,
    )

    override fun onCreate() {
        super.onCreate()

        presenter.setMobFeed(spawnerMobsList) {
            activeCheatsRepository.activateSpawnerCheat(player, it)
            close()
        }
    }
}
