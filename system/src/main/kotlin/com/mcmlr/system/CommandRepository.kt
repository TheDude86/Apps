package com.mcmlr.system

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor() {

    private val commandFlow = MutableStateFlow<CommandModel?>(null)

    fun emitCommand(model: CommandModel) = commandFlow.emitBackground(model)

    fun commandStream(): Flow<CommandModel> = commandFlow.filterNotNull()
}

data class CommandModel(val sender: CommandSender, val command: Command, val label: String, val args: Array<out String>)