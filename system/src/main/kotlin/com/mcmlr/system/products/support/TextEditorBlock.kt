package com.mcmlr.system.products.support

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.colorize
import com.mcmlr.blocks.core.underline
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class TextEditorBlock @Inject constructor(
    player: Player,
    camera: Camera,
): Block(player, camera) {
    companion object {
        const val TEXT_BUNDLE_KEY = "text"
    }

    private val view = TextEditorBlockViewController(player, camera)
    private val interactor = TextEditorInteractor(view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setTextModel(model: TextModel?) {
        val m = model ?: TextModel()
        interactor.model = m
    }
}

class TextEditorBlockViewController(
    player: Player,
    camera: Camera,
): NavigationViewController(player, camera), TextEditorPresenter {

    private lateinit var messageContainer: ListFeedView
    private lateinit var messageEditorContainer: ListFeedView
    private lateinit var textInputView: TextInputView
    private lateinit var sendButtonView: ButtonView
    private lateinit var editLinesButton: ButtonView
    private lateinit var finishButton: ButtonView

    private var lineSelectedCallback: (Int) -> Unit = {}

    override fun addFinishListener(listener: () -> Unit) = finishButton.addListener(listener)

    override fun addScrollListener(listener: (ScrollModel) -> Unit) = messageContainer.addScrollListener(listener)

    override fun removeScrollListener(listener: (ScrollModel) -> Unit) = messageContainer.removeScrollListener(listener)

    override fun setSendListener(listener: () -> Unit) = sendButtonView.addListener(listener)

    override fun setEditLinesListener(listener: () -> Unit) = editLinesButton.addListener(listener)

    override fun setTextInputListener(listener: (String) -> Unit) = textInputView.addTextChangedListener(listener)

    override fun setEditingLineListener(listener: (Int) -> Unit) {
        lineSelectedCallback = listener
    }

    override fun resetInput() {
        textInputView.setTextView("Write message here...")
    }

    override fun setFormattedInput(input: String) {
        textInputView.setTextView(input)
    }

    override fun setMessage(lines: TextModel, selectedLine: Int) {
        if (selectedLine > -1) {
            editLinesButton.setTextView("${ChatColor.GOLD}Finish edits")
        } else {
            editLinesButton.setTextView("${ChatColor.GOLD}Edit lines")
        }

        messageContainer.updateView {
            val text = if (lines.lines.isEmpty()) "${ChatColor.GRAY}${ChatColor.BOLD}Your message will be displayed here..." else lines.toMCFormattedText(selectedLine)

            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .margins(start = 50, top = 50),
                text = text,
                alignment = Alignment.LEFT,
                lineWidth = 500,
                size = 6,
            )
        }

        messageEditorContainer.updateView {
            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .margins(start = 50, top = 50),
                text = "${ChatColor.ITALIC}${ChatColor.BOLD}Edit Lines",
                alignment = Alignment.LEFT,
                size = 4,
            )

            lines.lines.forEachIndexed { index, _ ->
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .margins(start = 50),
                    text = "${index + 1}.${if (index == selectedLine) " (Editing)" else ""}",
                    highlightedText = "${ChatColor.BOLD}${index + 1}.${if (index == selectedLine) " (Editing)" else ""}",
                    alignment = Alignment.LEFT,
                    size = 8,
                ) {
                    lineSelectedCallback.invoke(index)
                }
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Write your message",
            size = 16,
        )

        messageContainer = addListFeedView(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignStartToStartOf(this)
                .alignTopToBottomOf(title)
                .alignEndToEndOf(this)
                .alignBottomToBottomOf(this)
                .margins(start = 700, 50, 700, 700),
            background = Color.fromARGB(64, 64, 64, 64),
        ) {
            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .margins(start = 50, top = 50),
                text = "${ChatColor.GRAY}${ChatColor.BOLD}Your message will be displayed here...",
                alignment = Alignment.LEFT,
                lineWidth = 500,
                size = 6,
            )
        }

        messageEditorContainer = addListFeedView(
            modifier = Modifier()
                .size(200, FILL_ALIGNMENT)
                .alignTopToTopOf(messageContainer)
                .alignEndToStartOf(messageContainer)
                .alignBottomToBottomOf(messageContainer)
                .margins(bottom = 50),
        ) {
            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .margins(start = 50, top = 50),
                text = "${ChatColor.ITALIC}${ChatColor.BOLD}Edit Lines",
                alignment = Alignment.LEFT,
                size = 4,
            )
        }

        editLinesButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(messageEditorContainer)
                .alignStartToStartOf(messageEditorContainer)
                .alignEndToEndOf(messageEditorContainer),
            text = "${ChatColor.GOLD}Edit lines",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Edit lines",
            size = 4,
        )

        val inputContainer = addViewContainer(
            Modifier()
                .size(FILL_ALIGNMENT, 125)
                .alignTopToBottomOf(messageContainer)
                .alignStartToStartOf(messageContainer)
                .alignEndToEndOf(messageContainer),
        ) {
            textInputView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .centerVertically()
                    .margins(start = 50),
                text = "Write message here...",
                highlightedText = "${ChatColor.BOLD}Write message here...",
                lineWidth = 430,
                size = 6,
            )

            sendButtonView = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignEndToEndOf(this)
                    .centerVertically()
                    .margins(end = 50),
                text = "${ChatColor.GOLD}Add",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Add",
            )
        }

        finishButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(inputContainer)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            text = "${ChatColor.GOLD}Finish",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Finish",
            size = 14,
        )
    }
}

interface TextEditorPresenter: Presenter {
    fun setSendListener(listener: () -> Unit)
    fun setEditLinesListener(listener: () -> Unit)
    fun addFinishListener(listener: () -> Unit)
    fun setTextInputListener(listener: (String) -> Unit)
    fun addScrollListener(listener: (ScrollModel) -> Unit)
    fun removeScrollListener(listener: (ScrollModel) -> Unit)
    fun setEditingLineListener(listener: (Int) -> Unit)

    fun setFormattedInput(input: String)
    fun setMessage(lines: TextModel, selectedLine: Int = -1)
    fun resetInput()
}

class TextEditorInteractor(
    private val presenter: TextEditorPresenter,
): Interactor(presenter) {

    var model = TextModel()

    private var editLines: Boolean = false
    private var editingLine = -1
    private var input: String = ""
    private var messageScrollListener: (ScrollModel) -> Unit = { event ->
        val newLine = editingLine + if (event.event == ScrollEvent.UP) -1 else 1
        editingLine = max(0, min(model.lines.size - 1, newLine))
        presenter.setMessage(model, editingLine)
    }

    override fun onCreate() {
        super.onCreate()

        editLines = false
        presenter.setMessage(model)

        presenter.setTextInputListener {
            input = it.replace("\\n", "\n").colorize()
            presenter.setFormattedInput(input)
        }

        presenter.setEditingLineListener {
            editingLine = it
            presenter.setMessage(model, editingLine)
        }

        presenter.setSendListener {
            if (input.isEmpty()) {
                return@setSendListener
            }

            if (editLines) {
                model.lines[editingLine] = input
            } else {
                model.lines.add(input)
            }

            presenter.setMessage(model, editingLine)
            presenter.resetInput()
            input = ""
        }

        presenter.setEditLinesListener {
            editLines = !editLines
            if (editLines) {
                editingLine = 0
                presenter.addScrollListener(messageScrollListener)
            } else {
                editingLine = -1
                presenter.removeScrollListener(messageScrollListener)
            }

            presenter.setMessage(model, editingLine)
        }

        presenter.addFinishListener {
            addBundleData(TextEditorBlock.TEXT_BUNDLE_KEY, model)
            routeBack()
        }
    }
}

data class TextModel(var lines: MutableList<String> = mutableListOf()) {
    fun toMCFormattedText(selectedLine: Int = -1): String {
        val messageBuilder = StringBuilder()
        lines.forEachIndexed { index, line ->
            if (index == selectedLine) {
                messageBuilder.append("${line.underline()} ${ChatColor.RESET}")
            } else {
                messageBuilder.append("$line ${ChatColor.RESET}")
            }
        }

        return messageBuilder.toString()
    }
}
