package com.mcmlr.system.products.support

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.bolden
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import javax.inject.Inject

class FileEditorBlock @Inject constructor(player: Player, origin: Location,): Block(player, origin) {
    private val view = FileEditorViewController(player, origin)
    private val interactor = FileEditorInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setFile(file: File) {
        interactor.setFile(file)
        view.title = file.nameWithoutExtension
    }
}

class FileEditorViewController(player: Player, origin: Location): NavigationViewController(player, origin), FileEditorPresenter {
    var title = "File"

    private lateinit var fileView: ListFeedView
    private lateinit var filePathContainer: ViewContainer
    private lateinit var modelListener: (Pair<String, Any>) -> Unit
    private lateinit var pathListener: (Any?) -> Unit
    private lateinit var saveListener: Listener

    private var inputView: TextInputView? = null
    private var inputLabelView: TextView? = null

    override fun setSaveListener(listener: Listener) {
        saveListener = listener
    }

    override fun setModelListener(listener: (Pair<String, Any>) -> Unit) {
        modelListener = listener
    }

    override fun setPathListener(listener: (Any?) -> Unit) {
        pathListener = listener
    }

    override fun setPrimitive(name: String, datum: Any, listener: TextListener) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val nameView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this)
                        .alignBottomToBottomOf(this)
                        .margins(start = 75),
                    alignment = Alignment.LEFT,
                    size = 6,
                    text = "${ChatColor.GOLD}${ChatColor.BOLD}$name:",
                )

                inputView = addTextInputView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(nameView)
                        .alignBottomToTopOf(nameView)
                        .margins(start = 100),
                    alignment = Alignment.LEFT,
                    size = 6,
                    text = datum.toString(),
                )

                inputView?.addTextChangedListener(listener)

                val saveButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignBottomToBottomOf(this)
                        .centerHorizontally()
                        .margins(bottom = 50),
                    text = "${ChatColor.GOLD}Save",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Save",
                    callback = saveListener,
                )

                inputLabelView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(saveButton)
                        .centerHorizontally()
                        .margins(top = 25),
                    size = 4,
                    text = "",
                )
            }
        })
    }

    override fun setSaveLabelVisible(isVisible: Boolean) {
        inputLabelView?.update(text = if (isVisible) "${ChatColor.GREEN}Value saved!" else "")
    }

    override fun setPath(models: List<Any>) {
        filePathContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                var nextButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .centerVertically(),
                    text = "${ChatColor.GOLD}$title${if (models.isNotEmpty()) " >" else ""}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$title${if (models.isNotEmpty()) " >" else ""}",
                    callback = object : Listener {
                        override fun invoke() {
                            pathListener.invoke(null)
                        }
                    }
                )

                for (i in 0..<models.size) {
                    val name = when(models[i]::class.java) {
                        MemorySection::class.java -> (models[i] as MemorySection).name
                        YMLListModel::class.java -> (models[i] as YMLListModel).name
                        YMLMapModel::class.java -> (models[i] as YMLMapModel).index
                        else -> models[i]
                    }

                    nextButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(nextButton)
                            .centerVertically()
                            .margins(start = 50),
                        text = "${ChatColor.GOLD}$name${if (i < models.size - 1) " >" else ""}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$name${if (i < models.size - 1) " >" else ""}",
                        callback = object : Listener {
                            override fun invoke() {
                                pathListener.invoke(models[i])
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setMap(modelMap: HashMap<String, Any?>) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                modelMap.keys.forEach {  key ->
                    val model = modelMap[key]
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .margins(start = 50, top = 100),
                        alignment = Alignment.LEFT,
                        lineWidth = 600,
                        size = 6,
                        text = makeMapLine(key, model),
                        highlightedText = makeMapLine(key, model).bolden(),
                        callback = object : Listener {
                            override fun invoke() {
                                model?.let {
                                    modelListener.invoke(Pair(key, it))
                                }
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setList(modelList: List<*>) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                modelList.forEachIndexed { i, it ->
                    val model = it ?: return@forEachIndexed

                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .margins(start = 50, top = 100),
                        alignment = Alignment.LEFT,
                        lineWidth = 600,
                        size = 6,
                        text = makeListLine(model),
                        highlightedText = makeListLine(model).bolden(),
                        callback = object : Listener {
                            override fun invoke() {
                                val data = if (model is LinkedHashMap<*, *>) {
                                    model[model.keys.first()]
                                } else {
                                    model
                                } ?: return
                                modelListener.invoke(Pair(i.toString(), data))
                            }
                        }
                    )

//                    addViewContainer(
//                        modifier = Modifier()
//                            .size(MATCH_PARENT, 100),
//                        content = object : ContextListener<ViewContainer>() {
//                            override fun ViewContainer.invoke() {
//                                val deleteButton = addButtonView(
//                                    modifier = Modifier()
//                                        .size(WRAP_CONTENT, WRAP_CONTENT)
//                                        .alignStartToStartOf(this)
//                                        .margins(start = 50, top = 100),
//                                    text = "${ChatColor.RED}\uD83D\uDDD1",
//                                    highlightedText = "${ChatColor.RED}${ChatColor.BOLD}\uD83D\uDDD1",
//                                    callback = object : Listener {
//                                        override fun invoke() {
//
//                                        }
//                                    }
//                                )
//
//                                addButtonView(
//                                    modifier = Modifier()
//                                        .size(WRAP_CONTENT, WRAP_CONTENT)
//                                        .alignStartToEndOf(deleteButton)
//                                        .alignTopToTopOf(deleteButton)
//                                        .alignBottomToBottomOf(deleteButton)
//                                        .margins(start = 100),
//                                    alignment = Alignment.LEFT,
//                                    lineWidth = 600,
//                                    size = 6,
//                                    text = makeListLine(model),
//                                    highlightedText = makeListLine(model).bolden(),
//                                    callback = object : Listener {
//                                        override fun invoke() {
//                                            val data = if (model is LinkedHashMap<*, *>) {
//                                                model[model.keys.first()]
//                                            } else {
//                                                model
//                                            } ?: return
//                                            modelListener.invoke(Pair(i.toString(), data))
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    )
                }
            }
        })
    }

    override fun setModel(lines: List<Pair<String, Any>>) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                lines.forEach {
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .margins(start = 50, top = 100),
                        alignment = Alignment.LEFT,
                        lineWidth = 600,
                        size = 6,
                        text = makeLine(it),
                        highlightedText = makeLine(it).bolden(),
                        callback = object : Listener {
                            override fun invoke() {
                                modelListener.invoke(it)
                            }
                        }
                    )
                }
            }
        })
    }

    private fun makeLine(element: Pair<String, Any>): String {
        val result = when (element.second::class.java) {
            MemorySection::class.java-> "▶"
            ArrayList::class.java -> "List"
            else -> element.second
        }

        return "${element.first.bolden()}: ${ChatColor.GOLD}$result"
    }

    private fun makeListLine(element: Any): String {
        return when (element::class.java) {
            LinkedHashMap::class.java -> "${(element as LinkedHashMap<*, *>).keys.first()}: ${ChatColor.GOLD}▶"
            else -> element.toString()
        }
    }

    private fun makeMapLine(key: String, data: Any?): String {
        val model = data ?: return "null"
        return when (model::class.java) {
            LinkedHashMap::class.java -> "${(model as LinkedHashMap<*, *>).keys.first()}: ${ChatColor.GOLD}▶"
            else -> "$key: ${ChatColor.GOLD}$model"
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}$title",
            size = 16,
        )

        fileView = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 200, bottom = 200),
        )

        filePathContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, 100)
                .alignStartToStartOf(fileView)
                .alignEndToEndOf(fileView)
                .alignBottomToTopOf(fileView),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }
}

interface FileEditorPresenter: Presenter {
    fun setPathListener(listener: (Any?) -> Unit)
    fun setModelListener(listener: (Pair<String, Any>) -> Unit)
    fun setSaveListener(listener: Listener)
    fun setModel(lines: List<Pair<String, Any>>)
    fun setList(modelList: List<*>)
    fun setMap(modelMap: HashMap<String, Any?>)
    fun setPath(models: List<Any>)
    fun setPrimitive(name: String, datum: Any, listener: TextListener)
    fun setSaveLabelVisible(isVisible: Boolean)
}

class FileEditorInteractor(
    private val player: Player,
    private val presenter: FileEditorPresenter,
): Interactor(presenter) {

    var editingFile: File? = null
    private var modelPath = mutableListOf<Any>()
    private var fieldName: String? = null
    private var newValue: Any? = null

    private lateinit var config: YamlConfiguration

    override fun onCreate() {
        super.onCreate()

        val file = editingFile ?: return
        config = YamlConfiguration.loadConfiguration(file)

        presenter.setPathListener {
            if (it == null) {
                modelPath.clear()
                loadRoot()
            } else {
                while (modelPath.isNotEmpty()) {
                    if (modelPath.removeLast() == it) break
                }

                loadNewObject(it)
            }
        }

        presenter.setModelListener {
            when (it.second::class.java) {
                MemorySection::class.java -> loadNewObject(it.second as MemorySection)
                ArrayList::class.java -> loadList(YMLListModel(it.first, it.second as ArrayList<Any?>))
                LinkedHashMap::class.java -> {
                    val data = it.second as? LinkedHashMap<String, Any?> ?: return@setModelListener
                    loadMap(YMLMapModel(it.first, data))
                }
                else -> {
                    fieldName = it.first
                    presenter.setPrimitive(it.first, it.second, object : TextListener {
                        override fun invoke(text: String) {
                            newValue = text.toIntOrNull() ?: text.toDoubleOrNull() ?: text.lowercase().toBooleanStrictOrNull() ?: when (text.lowercase()) {
                                "yes", "on" -> true
                                "no", "off" -> false
                                else -> text
                            }

                            presenter.setSaveLabelVisible(false)
                        }
                    })
                }
            }
        }

        presenter.setSaveListener(object : Listener {
            override fun invoke() {
                val model = modelPath.lastOrNull()

                if (model == null) {
                    val key = fieldName ?: return
                    config.set(key, newValue)
                    save()
                    presenter.setSaveLabelVisible(true)
                } else if (model is YMLListModel) {
                    val index = fieldName?.toIntOrNull() ?: return
                    model.data[index] = newValue
                    save()
                    presenter.setSaveLabelVisible(true)
                } else if (model is YMLMapModel) {
                    val key = fieldName ?: return
                    model.data[key] = newValue
                    save()
                    presenter.setSaveLabelVisible(true)
                } else if (model is MemorySection) {
                    val key = fieldName ?: return
                    model.set(key, newValue)
                    save()
                    presenter.setSaveLabelVisible(true)
                }
            }
        })

        loadRoot()
    }

    fun loadMap(model: YMLMapModel) {
        modelPath.add(model)

        presenter.setMap(model.data)
        presenter.setPath(modelPath)
    }

    fun loadList(model: YMLListModel) {
        modelPath.add(model)
        presenter.setList(model.data)
        presenter.setPath(modelPath)
    }

    fun loadNewObject(model: Any) {
        if (model is YMLListModel) {
            loadList(model)
        } else if (model is YMLMapModel) {
            loadMap(model)
        } else if (model is MemorySection) {
            modelPath.add(model)
            val lines = model.getKeys(false).mapNotNull {
                val result = model.get(it) ?: return@mapNotNull null
                Pair(it, result)
            }

            presenter.setModel(lines)
            presenter.setPath(modelPath)
        }
    }

    fun loadRoot() {
        val lines = config.getKeys(false).mapNotNull {
            val result = config.get(it) ?: return@mapNotNull null
            Pair(it, result)
        }

        presenter.setModel(lines)
        presenter.setPath(modelPath)
    }

    fun setFile(file: File) {
        editingFile = file
        modelPath.clear()
    }

    private fun save() {
        val file = editingFile ?: return
        val pathBuilder = StringBuilder()
        val modelPathIterator = modelPath.iterator()

        while (modelPathIterator.hasNext()) {
            val element = modelPathIterator.next()
            when (element::class.java) {
                MemorySection::class.java -> pathBuilder.append("${(element as MemorySection).name}.")
                YMLListModel::class.java -> {
                    val model = element as YMLListModel
                    pathBuilder.append(model.name)
                    config.set(pathBuilder.toString(), model.data)
                    config.save(file)

                    return
                }
                YMLMapModel::class.java -> {
                    val model = element as YMLMapModel
                    pathBuilder.append(model.index)
                    config.set(pathBuilder.toString(), model.data)
                    config.save(file)

                    return
                }
            }
        }

        config.save(file)
    }
}

data class YMLListModel(val name: String, val data: ArrayList<Any?>)
data class YMLMapModel(val index: String, val data: HashMap<String, Any?>)
