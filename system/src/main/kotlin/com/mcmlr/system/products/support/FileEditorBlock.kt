package com.mcmlr.system.products.support

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
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

    override fun setModelListener(listener: (Pair<String, Any>) -> Unit) {
        modelListener = listener
    }

    override fun setPathListener(listener: (Any?) -> Unit) {
        pathListener = listener
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

    override fun setMap(modelMap: Map<*, *>) {
        fileView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                modelMap.forEach {
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .margins(start = 50, top = 100),
                        alignment = Alignment.LEFT,
                        lineWidth = 600,
                        size = 6,
                        text = makeMapLine(it.key, it.value),
                        highlightedText = makeMapLine(it.key, it.value).bolden(),
                        callback = object : Listener {
                            override fun invoke() {
//                                modelListener.invoke(Pair(i.toString(), model))
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
                                modelListener.invoke(Pair(i.toString(), model))
                            }
                        }
                    )
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
            LinkedHashMap::class.java -> "Object: ${ChatColor.GOLD}▶"
            else -> element.toString()
        }
    }

    private fun makeMapLine(key: Any?, data: Any?): String {
        log(Log.DEBUG, "${key?.javaClass?.name} ${data?.javaClass?.name}")
        return "$key: ${ChatColor.GOLD}$data"
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
    fun setModel(lines: List<Pair<String, Any>>)
    fun setList(modelList: List<*>)
    fun setMap(modelMap: Map<*, *>)
    fun setPath(models: List<Any>)
}

class FileEditorInteractor(
    private val player: Player,
    private val presenter: FileEditorPresenter,
): Interactor(presenter) {

    var editingFile: File? = null
    private var modelPath = mutableListOf<Any>()

    override fun onCreate() {
        super.onCreate()

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
                ArrayList::class.java -> loadList(YMLListModel(it.first, it.second as List<*>))
                LinkedHashMap::class.java -> loadMap(YMLMapModel(it.first, it.second as Map<*, *>))
                else -> {

                }
            }
        }

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
        val file = editingFile ?: return
        val config = YamlConfiguration.loadConfiguration(file)


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
}

data class YMLListModel(val name: String, val data: List<*>)
data class YMLMapModel(val index: String, val data: Map<*, *>)
