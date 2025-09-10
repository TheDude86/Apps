package com.mcmlr.blocks.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

fun delay(dispatcher: CoroutineDispatcher = Dispatchers.IO, duration: Duration, callback: () -> Unit) = CoroutineScope(dispatcher).launch {
    delay(duration)
}.invokeOnCompletion {
    CoroutineScope(DudeDispatcher()).launch {
        callback.invoke()
    }
}

fun <T1, T2> Flow<T1>.withLatestFrom(flow: Flow<T2>): Flow<Pair<T1, T2>> = map { Pair(it, flow.first()) }

fun <T> MutableSharedFlow<T>.emitBackground(data: T) {
    CoroutineScope(Dispatchers.IO).launch { emit(data) }
}

fun <T> MutableSharedFlow<T>.emitForeground(data: T) {
    CoroutineScope(DudeDispatcher()).launch { emit(data) }
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

class DudeDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Apps")!!, block)
        } catch (_: Exception) { } //TODO: Fix crash when app is disabled
    }
}