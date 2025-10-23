package com.mcmlr.system.products.yaml

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.formattedLocalTime
import com.mcmlr.blocks.core.titlecase
import com.mcmlr.system.products.support.FileEditorBlock
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class YAMLBlock @Inject constructor(
    player: Player,
    origin: Location,
    resources: Resources,
    fileEditorBlock: FileEditorBlock,
): Block(player, origin) {
    companion object {
        val EDITABLE_FILE_TYPES = setOf("yml")
    }


    private val view = YAMLViewController(player, origin)
    private val interactor = YAMLInteractor(player, resources, view, fileEditorBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class YAMLViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), YAMLPresenter {

    private lateinit var appsFeed: ListFeedView
    private lateinit var filePathContainer: ViewContainer
    private lateinit var selectedFileContainer: ViewContainer

    override fun clearSelectedFile() {
        selectedFileContainer.updateView()
    }

    override fun setSelectedFile(file: File, callback: (File) -> Unit) {
        selectedFileContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val icon = if (YAMLBlock.EDITABLE_FILE_TYPES.contains(file.extension)) "\uD83D\uDCDD" else "\uD83D\uDCC4"
                val fileIcon = addTextView(
                    modifier = Modifier()
                        .size(100, 100)
                        .alignTopToTopOf(this)
                        .centerHorizontally()
                        .margins(top = 400),
                    size = 80,
                    text = icon,
                )

                val fileName = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToBottomOf(fileIcon)
                        .margins(start = 100, top = 100),
                    text = file.name.bolden(),
                )

                val fileSize = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToBottomOf(fileName)
                        .margins(start = 100, top = 25),
                    size = 7,
                    text = "${ChatColor.GRAY}${file.length() / 1024}KB",
                )

                val createdTime = Files.readAttributes<BasicFileAttributes>(file.toPath(), BasicFileAttributes::class.java).creationTime().toInstant().atZone(ZoneId.systemDefault())
                val createTimeString = "${createdTime.month.name.titlecase()} ${createdTime.dayOfMonth}, ${createdTime.year} at ${createdTime.formattedLocalTime()}"

                val fileCreated = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToBottomOf(fileSize)
                        .margins(start = 100, top = 35),
                    size = 7,
                    text = "${ChatColor.GRAY}Created - ${ChatColor.RESET}$createTimeString",
                )

                val modifiedTime = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault())
                val modifiedString = "${modifiedTime.month.name.titlecase()} ${modifiedTime.dayOfMonth}, ${createdTime.year} at ${createdTime.formattedLocalTime()}"

                val fileModified = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToBottomOf(fileCreated)
                        .margins(start = 100, top = 25),
                    size = 7,
                    text = "${ChatColor.GRAY}Modified - ${ChatColor.RESET}$modifiedString",
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(fileModified)
                        .alignBottomToBottomOf(this)
                        .centerHorizontally(),
                    text = "${ChatColor.GOLD}Open",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Open",
                    callback = object : Listener {
                        override fun invoke() {
                            callback.invoke(file)
                        }
                    }
                )
            }
        })
    }

    override fun setPath(directories: List<File>, callback: (File?) -> Unit) {
        filePathContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                var nextButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .centerVertically(),
                    text = "${ChatColor.GOLD}Plugins${if (directories.isNotEmpty()) " >" else ""}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Plugins${if (directories.isNotEmpty()) " >" else ""}",
                    callback = object : Listener {
                        override fun invoke() {
                            callback.invoke(null)
                        }
                    }
                )

                for (i in 0..<directories.size) {
                    nextButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(nextButton)
                            .centerVertically()
                            .margins(start = 50),
                        text = "${ChatColor.GOLD}${directories[i].name}${if (i < directories.size - 1) " >" else ""}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${directories[i].name}${if (i < directories.size - 1) " >" else ""}",
                        callback = object : Listener {
                            override fun invoke() {
                                callback.invoke(directories[i])
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setDirectory(directory: File, callback: (File) -> Unit) {
        appsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val directories = mutableListOf<File>()
                val files = mutableListOf<File>()
                directory.listFiles().forEach { if (it.isDirectory) directories.add(it) else files.add(it) }

                directories.forEach {
                    addViewContainer(
                        modifier = Modifier().size(MATCH_PARENT, 75),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    size = 5,
                                    maxLength = 100,
                                    text = "\uD83D\uDDC1 ${it.name}",
                                    background = Color.fromARGB(0, 0, 0 ,0),
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(it)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }

                files.forEach {
                    addViewContainer(
                        modifier = Modifier().size(MATCH_PARENT, 75),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val icon = if (YAMLBlock.EDITABLE_FILE_TYPES.contains(it.extension)) "\uD83D\uDCDD" else "\uD83D\uDCC4"
                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    size = 5,
                                    maxLength = 100,
                                    text = "$icon ${it.name}",
                                    background = Color.fromARGB(0, 0, 0 ,0),
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(it)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Files",
            size = 16,
        )

        appsFeed = addListFeedView(
            modifier = Modifier()
                .size(350, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .x(-800)
                .margins(top = 200, bottom = 200),
        )

        selectedFileContainer = addViewContainer(
            modifier = Modifier()
                .size(800, FILL_ALIGNMENT)
                .alignStartToEndOf(appsFeed)
                .alignTopToTopOf(appsFeed)
                .alignBottomToBottomOf(appsFeed),
            background = Color.fromARGB(0, 0, 0, 0),
        )

        filePathContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, 100)
                .alignStartToStartOf(appsFeed)
                .alignEndToEndOf(appsFeed)
                .alignBottomToTopOf(appsFeed),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }
}

interface YAMLPresenter: Presenter {
    fun setDirectory(directory: File, callback: (File) -> Unit)
    fun setPath(directories: List<File>, callback: (File?) -> Unit)
    fun setSelectedFile(file: File, callback: (File) -> Unit)
    fun clearSelectedFile()
}

class YAMLInteractor(
    private val player: Player,
    private val resources: Resources,
    private val presenter: YAMLPresenter,
    private val fileEditorBlock: FileEditorBlock,
): Interactor(presenter) {

    private var currentDirectory = resources.dataFolder().parentFile
    private var directoryPath = mutableListOf<File>()
    private var selectedFile: File? = null

    val backCallback: (File?) -> Unit = {
        presenter.clearSelectedFile()
        if (it == null) {
            currentDirectory = resources.dataFolder().parentFile
            directoryPath = mutableListOf<File>()
            presenter.setDirectory(currentDirectory, callback)
            presenter.setPath(directoryPath, backCallback)

        } else if (it.isDirectory) {
            val newDirectoryPath = mutableListOf<File>()
            while (true) {
                if (directoryPath.isEmpty()) break

                val directory = directoryPath.removeFirst()
                newDirectoryPath.add(directory)

                if (directory == it) break
            }

            directoryPath = newDirectoryPath
            updateDirectoryState(it)
        }
    }

    val callback: (File) -> Unit = {
        if (it.isDirectory) {
            directoryPath.add(it)
            updateDirectoryState(it)
        } else {
            selectedFile = it
            presenter.setSelectedFile(it) {
                fileEditorBlock.setFile(it)
                routeTo(fileEditorBlock)
            }
        }
    }

    private fun updateDirectoryState(currentDirectory: File) {
        this.currentDirectory = currentDirectory
        presenter.setDirectory(currentDirectory, callback)
        presenter.setPath(directoryPath, backCallback)
    }

    override fun onCreate() {
        super.onCreate()

        currentDirectory.path
        presenter.setDirectory(currentDirectory, callback)
        presenter.setPath(directoryPath, backCallback)

        selectedFile?.let {
            presenter.setSelectedFile(it) {
                fileEditorBlock.setFile(it)
                routeTo(fileEditorBlock)
            }
        }
    }
}