package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.AppManager
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.log
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

abstract class App(player: Player): BaseApp(player) {
    lateinit var inputRepository: InputRepository

    protected lateinit var appManager: AppManager

    fun configure(
        appManager: AppManager,
        environment: BaseEnvironment<BaseApp>,
        parentApp: BaseApp,
        inputRepository: InputRepository,
        deeplink: String?,
        origin: Location,
    ) {
        this.appManager = appManager
        this.parentEnvironment = environment
        this.parentApp = parentApp
        this.inputRepository = inputRepository
        this.deeplink = deeplink
        this.origin = origin
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorEvent) {
        head.cursorEvent(displays, cursor, event)
    }

    fun scrollEvent(event: ScrollModel, isChild: Boolean = false) {
        head.scrollEvent(event, isChild)
    }

    fun textInputEvent(event: AsyncPlayerChatEvent) {
        head.textInputEvent(event)
    }

    override fun launchApp(app: Environment<App>, deeplink: String?) {
        appManager.launch(app, deeplink)
    }

    override fun close() {
        appManager.notifyShutdown()
        super.close()
    }

    override fun setScrollState(isScrolling: Boolean) {
        inputRepository.updateUserScrollState(player.uniqueId, isScrolling)
    }

    override fun setInputState(getInput: Boolean) {
        inputRepository.updateUserInputState(player.uniqueId, getInput)
    }

    override fun hasParent(): Boolean = true

    override fun routeBack() {
        val parent = head.parent
        if (parent == null) {
            onClose()
            head.onClose()
            clear()
            appManager.closeApp()
        } else {
            parent.onCreate()
            head.onClose()
            head = parent
        }
    }
}