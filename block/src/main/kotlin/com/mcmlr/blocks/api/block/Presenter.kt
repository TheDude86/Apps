package com.mcmlr.blocks.api.block

import org.bukkit.Location

interface Presenter {
    fun updateOrigin(origin: Location)

    fun render()

    fun createView()
}

@FunctionalInterface
interface Listener {
    fun invoke()
}

@FunctionalInterface
interface TextListener {
    fun invoke(text: String)
}

@FunctionalInterface
abstract class ContextListener<T> {
    fun invokeContext(scope: T) {
        scope.apply {
            invoke()
        }
    }

    abstract fun T.invoke()
}

class EmptyListener: Listener {
    override fun invoke() {}
}

class EmptyTextListener: TextListener {
    override fun invoke(text: String) {}
}

class EmptyContextListener<T>: ContextListener<T>() {
    override fun T.invoke() {}
}
