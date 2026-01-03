package com.mcmlr.system

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.system.ConfirmationBlock.Companion.CONFIRMATION_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class ConfirmationBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    companion object {
        const val CONFIRMATION_BUNDLE_KEY = "confirmation"
    }

    private val view = ConfirmationViewController(player, origin)
    private val interactor = ConfirmationInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setConfirmationModel(model: ConfirmationModel) {
        interactor.setConfirmationModel(model)
    }
}

class ConfirmationViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), ConfirmationPresenter {

    private lateinit var message: TextView
    private lateinit var acceptButton: ButtonView
    private lateinit var rejectButton: ButtonView

    override fun setAcceptListener(listener: Listener) {
        acceptButton.addListener(listener)
    }

    override fun setRejectListener(listener: Listener) {
        rejectButton.addListener(listener)
    }

    override fun setState(model: ConfirmationModel) {
        message.update(text = model.message)
        acceptButton.update(text = model.accept)
        rejectButton.update(text = model.reject)
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Confirm",
            size = 16,
        )

        message = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
            lineWidth = 150,
            size = 16,
            text = "",
        )

        acceptButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(message)
                .x(300)
                .margins(top = 100),
            text = "",
        )

        rejectButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(message)
                .x(-300)
                .margins(top = 100),
            text = "",
        )
    }

}

interface ConfirmationPresenter: Presenter {
    fun setState(model: ConfirmationModel)

    fun setAcceptListener(listener: Listener)

    fun setRejectListener(listener: Listener)
}

class ConfirmationInteractor(
    private val player: Player,
    private val presenter: ConfirmationPresenter,
): Interactor(presenter) {

    private var confirmationModel: ConfirmationModel? = null

    fun setConfirmationModel(model: ConfirmationModel) {
        confirmationModel = model
    }

    override fun onCreate() {
        super.onCreate()

        val model = confirmationModel ?: return
        presenter.setState(model)

        presenter.setAcceptListener(object : Listener {
            override fun invoke() {
                addBundleData(CONFIRMATION_BUNDLE_KEY, ConfirmationResponse.ACCEPT)
                routeBack()
            }
        })

        presenter.setRejectListener(object : Listener {
            override fun invoke() {
                addBundleData(CONFIRMATION_BUNDLE_KEY, ConfirmationResponse.REJECT)
                routeBack()
            }
        })
    }
}

data class ConfirmationModel(
    val message: String,
    val accept: String = "${ChatColor.GOLD}Confirm",
    val reject: String = "${ChatColor.GOLD}Cancel",
)

enum class ConfirmationResponse {
    ACCEPT,
    REJECT,
}
