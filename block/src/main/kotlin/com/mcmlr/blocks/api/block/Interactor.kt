package com.mcmlr.blocks.api.block

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.plugin.PluginManager
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.FlowDisposer
import org.bukkit.Location

abstract class Interactor(private val basePresenter: Presenter): FlowDisposer() {

    lateinit var context: Context

    private var isChild = false
    private val pluginManagers = mutableListOf<PluginManager<*>>()

    protected lateinit var router: Router

    open fun onCreate() {
        basePresenter.createView()
        basePresenter.render()
        clearBundle()
    }

    open fun onPause() {}

    open fun onResume(newOrigin: Location?) {
        if (newOrigin != null) basePresenter.updateOrigin(newOrigin)
        basePresenter.render()
        clearBundle()
    }

    open fun onClose() {
        clear()
    }

    fun <T> registerPluginManager(manager: PluginManager<T>) {
        manager.register()
        pluginManagers.add(manager)
    }

    fun attachChild(child: Block, parent: ViewContainer) {
        router.attachChild(child, parent)
    }

    fun detachChild(child: Block) {
        router.detachChild(child)
    }

    fun launchApp(app: Environment<App>, deeplink: String? = null) = router.launchApp(app, deeplink)

    fun launchAppConfig(app: ConfigurableEnvironment<ConfigurableApp>) = router.launchAppConfig(app)

    fun routeTo(block: Block, callback: RouteToCallback? = null) = router.routeTo(block, callback)

    fun routeBack() = router.routeBack()

    fun close() = router.close()

    fun minimize() = router.minimize()

    fun maximize() = router.maximize()

    fun configure(context: Context, router: Router, isChild: Boolean) {
        this.context = context
        this.router = router
        this.isChild = isChild
    }

    fun addBundleData(key: String, data: Any) = router.bundle.add(key, data)

    fun clearBundle() = router.bundle.clear()
}

class EmptyInteractor: Interactor(EmptyPresenter())

class EmptyPresenter: Presenter {
    override fun render() {}

    override fun createView() {}

    override fun updateOrigin(origin: Location) {}
}
