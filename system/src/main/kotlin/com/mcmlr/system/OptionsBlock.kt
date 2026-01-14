package com.mcmlr.system

import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.OptionsBlock.Companion.OPTION_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class OptionsBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    companion object {
        const val OPTION_BUNDLE_KEY = "option"
    }

    private val view = OptionsViewController(player, origin)
    private val interactor = OptionsInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setOptions(optionsModel: OptionsModel) {
        interactor.setOptions(optionsModel)
    }
}

class OptionsViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), OptionsPresenter {

    private lateinit var title: TextView
    private lateinit var contentFeed: ListFeedView

    override fun setOptions(optionsModel: OptionsModel, callback: (OptionRowModel) -> Unit) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                optionsModel.actions.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                callback.invoke(it)
                            }
                        },

                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {

                                val description = it.description
                                if (description == null) {
                                    val title = addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(this)
                                            .centerVertically()
                                            .margins(start = 50),
                                        size = 6,
                                        maxLength = 600,
                                        text = it.action,
                                    )
                                } else {
                                    val title = addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(this)
                                            .alignTopToTopOf(this)
                                            .margins(start = 50, top = 30),
                                        size = 6,
                                        maxLength = 600,
                                        text = it.action,
                                    )

                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(title)
                                            .alignTopToBottomOf(title),
                                        size = 4,
                                        maxLength = 600,
                                        text = description,
                                    )
                                }

                            }
                        }
                    )
                }

            }
        })
    }

    override fun createView() {
        super.createView()
        title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Options",
            size = 16,
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 300, bottom = 300)
        )
    }

}

interface OptionsPresenter: Presenter {
    fun setOptions(optionsModel: OptionsModel, callback: (OptionRowModel) -> Unit)
}

class OptionsInteractor(
    private val player: Player,
    private val presenter: OptionsPresenter,
): Interactor(presenter) {

    private var model: OptionsModel? = null

    fun setOptions(optionsModel: OptionsModel) {
        model = optionsModel
    }

    override fun onCreate() {
        super.onCreate()

        val model = model ?: return
        presenter.setOptions(model) {
            addBundleData(OPTION_BUNDLE_KEY, it.action)
            routeBack()
        }
    }
}

data class OptionsModel(
    val title: String,
    val actions: List<OptionRowModel>,
)

data class OptionRowModel(
    val action: String,
    val description: String? = null,
)
