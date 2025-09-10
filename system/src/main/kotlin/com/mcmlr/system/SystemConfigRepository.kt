package com.mcmlr.system

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.AppConfigModel
import com.mcmlr.blocks.api.data.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemConfigRepository @Inject constructor(
    resources: Resources,
): Repository<AppConfigModel>(resources.dataFolder()) {

    init {
        loadModel("", "config", AppConfigModel())
    }

    fun completeSetup(enabledAppNames: List<String>) = save {
        model.setupComplete = true
        model.enabledApps = enabledAppNames
    }

    fun updateServerTitle(title: String) = save {
        model.title = title
    }

    fun toggleUsePermissions() = save {
        model.usePermissions = !model.usePermissions
    }
}