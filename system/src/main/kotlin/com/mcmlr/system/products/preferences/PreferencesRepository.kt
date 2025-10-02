package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.products.data.ApplicationsRepository
import org.bukkit.entity.Player
import javax.inject.Inject

@AppScope
class PreferencesRepository @Inject constructor(
    resources: Resources,
    private val player: Player,
    private val applicationsRepository: ApplicationsRepository,
): Repository<PreferencesModel>(resources.dataFolder()) {

    private var editFavoriteIndex = 0

    init {
        loadModel("Preferences/${player.uniqueId}", "preferences", PreferencesModel())
    }

    fun setFavorite(model: BaseEnvironment<BaseApp>) = save {
        if (editFavoriteIndex < this.model.favoriteApps.size) {
            this.model.favoriteApps[editFavoriteIndex] = model.name()
        } else {
            this.model.favoriteApps.add(model.name())
        }
    }

    fun removeFavorite() = save {
        this.model.favoriteApps.removeAt(editFavoriteIndex)
    }

    fun getFavorites(player: Player): List<Environment<App>> = model.favoriteApps.mapNotNull { applicationsRepository.getApp(it, player) }

    fun getSelectedFavorite(player: Player): BaseEnvironment<BaseApp>? = if (editFavoriteIndex < model.favoriteApps.size) {
        applicationsRepository.getApp(model.favoriteApps[editFavoriteIndex], player)
    } else {
        null
    }

    fun setEditFavoriteIndex(index: Int) {
        editFavoriteIndex = index
    }

}

data class PreferencesModel(
    val favoriteApps: MutableList<String> = mutableListOf("Preferences"),
): ConfigModel()
