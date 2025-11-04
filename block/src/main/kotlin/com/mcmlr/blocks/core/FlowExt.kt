package com.mcmlr.blocks.core

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

fun delay(dispatcher: CoroutineDispatcher = Dispatchers.IO, duration: Duration, player: Player? = null, callback: Listener) = CoroutineScope(dispatcher).launch {
    delay(duration)
}.invokeOnCompletion {
    CoroutineScope(DudeDispatcher(player)).launch {
        callback.invoke()
    }
}

fun <T1, T2> Flow<T1>.withLatestFrom(flow: Flow<T2>): Flow<Pair<T1, T2>> = map { Pair(it, flow.first()) }

fun <T> MutableSharedFlow<T>.emitBackground(data: T) {
    CoroutineScope(Dispatchers.IO).launch { emit(data) }
}

fun <T> MutableSharedFlow<T>.emitForeground(data: T, player: Player? = null) {
    CoroutineScope(DudeDispatcher(player)).launch { emit(data) }
}

fun <T> Flow<T>.collectFirst(dispatcher: CoroutineDispatcher = Dispatchers.IO, callback: Flow<T>.(T) -> Unit) {
    val callbackWrapper: (T) -> Unit = { callback.invoke(this, it) }
    CoroutineScope(dispatcher).launch {
        collectLatest { datum ->
            callbackWrapper.invoke(datum)
            cancel()
        }

    }
}

fun <T> Flow<T>.collectOn(dispatcher: CoroutineDispatcher, action: suspend (T) -> Unit) {
    CoroutineScope(dispatcher).launch { action.invoke(first()) }
}

fun <T> Flow<T>.collectOn(dispatcher: CoroutineDispatcher) = Pair(CoroutineScope(dispatcher), this)

fun <T> Pair<CoroutineScope, Flow<T>>.collectLatest(action: suspend (value: T) -> Unit): Job = first.launch { second.collectLatest(action) }

fun Job.disposeOn(collection: String = FlowDisposer.DEFAULT, disposer: FlowDisposer) {
    disposer.addJob(collection, this)
}

class DudeDispatcher(private val player: Player? = null): CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (player != null) {
                Scheduler(Bukkit.getPluginManager().getPlugin("Apps")!!).run(block, player)
            } else {
                Scheduler(Bukkit.getPluginManager().getPlugin("Apps")!!).run(block)
            }
        } catch (_: Exception) {} //TODO: Fix crash when app is disabled
    }
}