package com.mcmlr.system.products.recipe

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.fromMCItem
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import javax.inject.Inject

class RecipesBlock @Inject constructor(
    player: Player,
    origin: Location,
): Block(player, origin) {
    private val view = RecipesViewController(player, origin)
    private val interactor = RecipesInteractor(view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class RecipesViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), RecipesPresenter {
    companion object {
        private const val RECIPE_ITEM_SIZE = 40
    }

    private lateinit var craftingSlots: List<ViewContainer>
    private lateinit var searchButton: TextInputView
    private lateinit var feedView: ListFeedView

    override fun addSearchListener(listener: TextListener) = searchButton.addTextChangedListener(listener)

    override fun setFeed(recipes: List<Recipe>, itemCallback: (Recipe) -> Unit) {
        feedView.updateView {
            for (i in recipes.indices step 6) {
                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 100),
                    background = Color.fromARGB(0, 0, 0, 0),
                ) {
                    for (j in 0..5) {
                        if (i + j >= recipes.size) break
                        val recipe = recipes[i + j]

                        addItemButtonView(
                            modifier = Modifier()
                                .position(-500 + (200 * j), 0)
                                .size(73, 73),
                            item = recipe.result,
                            callback = object : Listener {
                                override fun invoke() {
                                    itemCallback.invoke(recipe)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun setShapedRecipe(shape: Array<String>, recipe: Map<Char, ItemStack>) {
        craftingSlots.forEach { it.updateView {  } }

        shape.forEachIndexed { rowIndex, row ->
            val rowIndexOffset = rowIndex * 3
            row.forEachIndexed row@ { slotIndex, item ->
                val recipeItem = recipe[item] ?: return@row
                craftingSlots[rowIndexOffset + slotIndex].updateView {
                    addItemView(
                        modifier = Modifier()
                            .size(RECIPE_ITEM_SIZE, RECIPE_ITEM_SIZE)
                            .center(),
                        item = recipeItem
                    )
                }
            }
        }
    }

    override fun setShapelessRecipe(recipe: List<ItemStack>) {
        craftingSlots.forEach { it.updateView {  } }

        recipe.forEachIndexed { index, item ->
            craftingSlots[index].updateView {
                addItemView(
                    modifier = Modifier()
                        .size(RECIPE_ITEM_SIZE, RECIPE_ITEM_SIZE)
                        .center(),
                    item = item
                )
            }
        }
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Recipes",
            size = 16,
        )

        val craftingView = addViewContainer(
            modifier = Modifier()
                .size(170, 170)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 30),
            background = Color.fromARGB(0, 0, 0, 0),
        ) {
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

        searchButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(craftingView)
                .centerHorizontally()
                .margins(top = 30),
            size = 8,
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}\uD83D\uDD0D Search for items or blocks...",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}\uD83D\uDD0D Search for items or blocks...",
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

    fun setFeed(recipes: List<Recipe>, itemCallback: (Recipe) -> Unit)

    fun setShapedRecipe(shape: Array<String>, recipe: Map<Char, ItemStack>)

    fun setShapelessRecipe(recipe: List<ItemStack>)
}

class RecipesInteractor(
    private val presenter: RecipesPresenter,
): Interactor(presenter) {

    private val recipes = mutableListOf<Recipe>()
    private val feedCallback: (Recipe) -> Unit = { recipe ->
        when (recipe) {
            is ShapedRecipe -> presenter.setShapedRecipe(recipe.shape, recipe.ingredientMap)

            is ShapelessRecipe -> presenter.setShapelessRecipe(recipe.ingredientList)

            is FurnaceRecipe -> {

            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        recipes.clear()
        Bukkit.getServer().recipeIterator().forEach {
            if (it is ShapedRecipe || it is ShapelessRecipe) recipes.add(it)
        }

        presenter.addSearchListener(object : TextListener {
            override fun invoke(text: String) {
                presenter.setFeed(recipes.filter { it.result.type.name.fromMCItem().lowercase().contains(text.lowercase()) }, feedCallback)
            }
        })

        presenter.setFeed(recipes, feedCallback)
    }
}