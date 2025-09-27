package com.mcmlr.system.products.cheats

import com.mcmlr.blocks.core.emitBackground
import com.mcmlr.system.dagger.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@AppScope
class SelectedCheatRepository @Inject constructor() {

    private val selectedCheatFlow: MutableStateFlow<CheatType?> = MutableStateFlow(null)

    fun setSelectedCheat(cheat: CheatType) {
        selectedCheatFlow.emitBackground(cheat)
    }

    fun getSelectedCheatStream(): Flow<CheatType> = selectedCheatFlow.filterNotNull()
}