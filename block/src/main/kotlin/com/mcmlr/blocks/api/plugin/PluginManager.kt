package com.mcmlr.blocks.api.plugin

abstract class PluginManager<T>(private var executionType: PluginExecutionType = PluginExecutionType.FIRST) {
    private lateinit var plugins: HashSet<Plugin<T>>

    fun register() {
        plugins = setPlugins()
    }

    abstract fun setPlugins(): HashSet<Plugin<T>>

    fun execute(data: T) {
        plugins.forEach {
            if (it.isApplicable(data)) {
                it.execute(data)

                if (executionType == PluginExecutionType.FIRST) return
            }
        }
    }
}

enum class PluginExecutionType {
    FIRST,
    ALL,
}
