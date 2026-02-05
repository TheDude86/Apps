package com.mcmlr.system.products.recipe

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.fromMCItem
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine
import net.momirealms.craftengine.core.item.CustomItem
import net.momirealms.craftengine.core.item.recipe.CustomShapedRecipe
import net.momirealms.craftengine.core.item.recipe.CustomShapelessRecipe
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Player
import org.bukkit.inventory.*
import javax.inject.Inject

class RecipesBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    private val view = RecipesViewController(player, origin)
    private val interactor = RecipesInteractor(view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class RecipesViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), RecipesPresenter {
    companion object {
        private const val RECIPE_ITEM_SIZE = 40
    }

    private lateinit var craftingSlots: List<ViewContainer>
    private lateinit var searchButton: TextInputView
    private lateinit var feedView: ListFeedView

    override fun addSearchListener(listener: TextListener) = searchButton.addTextChangedListener(listener)

    override fun setFeed(recipes: List<DudeRecipe>, itemCallback: (DudeRecipe) -> Unit) {
        feedView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                for (i in recipes.indices step 6) {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 100),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                for (j in 0..5) {
                                    if (i + j >= recipes.size) break
                                    val recipe = recipes[i + j]

                                    val item = when {
                                        recipe.vanillaRecipe != null -> recipe.vanillaRecipe.result
                                        recipe.craftEngineRecipe != null -> recipe.craftEngineRecipe.buildItemStack()
                                        else -> null
                                    }

                                    addItemButtonView(
                                        modifier = Modifier()
                                            .position(-500 + (200 * j), 0)
                                            .size(73, 73),
                                        item = item,
                                        callback = object : Listener {
                                            override fun invoke() {
                                                itemCallback.invoke(recipe)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setShapedRecipe(shape: Array<String>, recipe: Map<Char, ItemStack>) {
        craftingSlots.forEach { it.updateView(EmptyContextListener<ViewContainer>()) }

        shape.forEachIndexed { rowIndex, row ->
            val rowIndexOffset = rowIndex * 3
            row.forEachIndexed row@ { slotIndex, item ->
                val recipeItem = recipe[item] ?: return@row
                craftingSlots[rowIndexOffset + slotIndex].updateView(object : ContextListener<ViewContainer>() {
                    override fun ViewContainer.invoke() {
                        addItemView(
                            modifier = Modifier()
                                .size(RECIPE_ITEM_SIZE, RECIPE_ITEM_SIZE)
                                .center(),
                            item = recipeItem
                        )
                    }
                })
            }
        }
    }

    override fun setShapelessRecipe(recipe: List<ItemStack>) {
        craftingSlots.forEach { it.updateView(EmptyContextListener<ViewContainer>()) }

        recipe.forEachIndexed { index, item ->
            craftingSlots[index].updateView(object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    addItemView(
                        modifier = Modifier()
                            .size(RECIPE_ITEM_SIZE, RECIPE_ITEM_SIZE)
                            .center(),
                        item = item
                    )
                }
            })
        }
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToStartOf(this)
                .margins(top = 250, start = 970),
            text = R.getString(player, S.RECIPES_TITLE.resource()),
            size = 16,
        )

        val craftingView = addViewContainer(
            modifier = Modifier()
                .size(170, 170)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 30),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    val slotOne = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(this)
                            .alignStartToStartOf(this),
                    )

                    val slotTwo = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotOne)
                            .alignStartToEndOf(slotOne)
                            .margins(start = 10),
                    )

                    val slotThree = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotTwo)
                            .alignStartToEndOf(slotTwo)
                            .margins(start = 10),
                    )

                    val slotFour = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignStartToStartOf(slotOne)
                            .alignTopToBottomOf(slotOne)
                            .margins(top = 10),
                    )

                    val slotFive = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotFour)
                            .alignStartToEndOf(slotFour)
                            .margins(start = 10),
                    )

                    val slotSix = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotFive)
                            .alignStartToEndOf(slotFive)
                            .margins(start = 10),
                    )

                    val slotSeven = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignStartToStartOf(slotFour)
                            .alignTopToBottomOf(slotFour)
                            .margins(top = 10),
                    )

                    val slotEight = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotSeven)
                            .alignStartToEndOf(slotSeven)
                            .margins(start = 10),
                    )

                    val slotNine = addViewContainer(
                        modifier = Modifier()
                            .size(50, 50)
                            .alignTopToTopOf(slotEight)
                            .alignStartToEndOf(slotEight)
                            .margins(start = 10),
                    )

                    craftingSlots = listOf(
                        slotOne, slotTwo, slotThree,
                        slotFour, slotFive, slotSix,
                        slotSeven, slotEight, slotNine
                    )
                }
            }
        )

        searchButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(craftingView)
                .centerHorizontally()
                .margins(top = 30),
            size = 8,
            text = R.getString(player, S.SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        feedView = addListFeedView(
            modifier = Modifier()
                .size(800, 500)
                .alignTopToBottomOf(searchButton)
                .centerHorizontally()
                .margins(top = 100, bottom = 0),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }

}

interface RecipesPresenter: Presenter {
    fun addSearchListener(listener: TextListener)

    fun setFeed(recipes: List<DudeRecipe>, itemCallback: (DudeRecipe) -> Unit)

    fun setShapedRecipe(shape: Array<String>, recipe: Map<Char, ItemStack>)

    fun setShapelessRecipe(recipe: List<ItemStack>)
}

class RecipesInteractor(
    private val presenter: RecipesPresenter,
): Interactor(presenter) {

    private val recipes = mutableListOf<DudeRecipe>()
    private val feedCallback: (DudeRecipe) -> Unit = { recipe ->
        when {
            recipe.vanillaRecipe != null -> {

                when (recipe.vanillaRecipe) {
                    is ShapedRecipe -> presenter.setShapedRecipe(recipe.vanillaRecipe.shape, recipe.vanillaRecipe.ingredientMap)

                    is ShapelessRecipe -> presenter.setShapelessRecipe(recipe.vanillaRecipe.ingredientList)

                    is FurnaceRecipe -> {

                    }
                }
            }

            recipe.craftEngineRecipe != null -> {
                val craftEngineRecipe = BukkitCraftEngine.instance().recipeManager<ItemStack>().recipeById(recipe.craftEngineRecipe.id()).get()

                when(craftEngineRecipe) {
                    is CustomShapedRecipe<*> -> {
                        val ingredients = mutableMapOf<Char, ItemStack>()
                        craftEngineRecipe.pattern().ingredients().forEach {
                            val key = it.key
                            val value = it.value
                            val ingredient = if (value.items().isNotEmpty()) value.items().first() else value.minecraftItems().firstOrNull() ?: return@forEach
                            val item = BukkitCraftEngine.instance().itemManager().buildItemStack(ingredient.key(), null) ?: return@forEach

                            ingredients[key] = item
                        }

                        presenter.setShapedRecipe(craftEngineRecipe.pattern().pattern(), ingredients)
                    }

                    is CustomShapelessRecipe<*> -> {
                        updateCraftEngineShapelessRecipe(craftEngineRecipe)
                    }

                    is CustomSmithingTransformRecipe<*> -> {

//                        craftEngineRecipe.ingredientsInUse().forEach {
//                            it.items().forEach {
//                                log(Log.DEBUG, it.key().value)
//                            }
//
//                            it.minecraftItems().forEach {
//                                log(Log.ASSERT, it.key().value)
//                            }
//                        }

                        updateCraftEngineShapelessRecipe(craftEngineRecipe)
                    }
                }
            }
        }
    }

    private fun updateCraftEngineShapelessRecipe(recipe: net.momirealms.craftengine.core.item.recipe.Recipe<*>) {
        val ingredients = mutableListOf<ItemStack>()
        recipe.ingredientsInUse().forEach {
            val ingredient = if (it.items().isNotEmpty()) it.items().first() else it.minecraftItems().firstOrNull() ?: return@forEach
            val item = CraftEngineItems.byId(ingredient.key())?.buildItemStack() ?: return@forEach
            ingredients.add(item)
        }

        presenter.setShapelessRecipe(ingredients)
    }

    override fun onCreate() {
        super.onCreate()

        recipes.clear()

//        CraftEngineItems
//            .loadedItems()
//            .values
//            .filter {
//                val optional = BukkitCraftEngine.instance().recipeManager<ItemStack>().recipeById(it.id())
//                if (optional.isPresent) {
//                    val recipe = optional.get()
//                    recipe is CustomShapedRecipe<*> || recipe is CustomShapelessRecipe<*>
//                } else {
//                    false
//                }
//            }
//            .forEach {
//                recipes.add(DudeRecipe(craftEngineRecipe = it))
//            }

        Bukkit.getServer().recipeIterator().forEach {
            if (it is ShapedRecipe || it is ShapelessRecipe) recipes.add(DudeRecipe(vanillaRecipe = it))
        }

        presenter.addSearchListener(object : TextListener {
            override fun invoke(text: String) {
                presenter.setFeed(recipes.filter {
                    when {
                        it.vanillaRecipe != null -> {
                            it.vanillaRecipe.result.type.name.fromMCItem().lowercase().contains(text.lowercase())
                        }
                        it.craftEngineRecipe != null -> {
                            it.craftEngineRecipe.id().value.fromMCItem().lowercase().contains(text.lowercase())
                        }
                        else -> false
                    }
                }, feedCallback)
            }
        })

        presenter.setFeed(recipes, feedCallback)
    }
}

data class DudeRecipe(val vanillaRecipe: Recipe? = null, val craftEngineRecipe: CustomItem<ItemStack>? = null)
