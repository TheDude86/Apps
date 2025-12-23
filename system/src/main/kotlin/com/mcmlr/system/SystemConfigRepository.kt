package com.mcmlr.system

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.AppConfigModel
import com.mcmlr.blocks.api.data.Repository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemConfigRepository @Inject constructor(
    resources: Resources,
): Repository<AppConfigModel>(resources.dataFolder()) {
    companion object {
        val supportedLocals = listOf(
            Locale.US,
            Locale.UK,
            Locale.CANADA,
            Locale("de", "de"),
            Locale("fr", "fr"),
            Locale("es", "es"),
            Locale("nl", "nl"),
            Locale("pl", "pl"),
            Locale("tr", "tr"),
            Locale("el", "gr"),
        )
    }

    init {
        loadModel("", "config", AppConfigModel())
    }

    fun completeSetup(enabledAppNames: List<String>) = save {
        model.setupComplete = true
        model.enabledApps = enabledAppNames
    }

    fun saveEnabledApps(enabledAppNames: List<String>) = save {
        model.enabledApps = enabledAppNames
    }

    fun updateServerTitle(title: String) = save {
        model.title = title
    }

    fun toggleUsePermissions() = save {
        model.usePermissions = !model.usePermissions
    }

    fun updateDefaultLanguage(locale: Locale) = save {
        model.defaultLanguage = locale.toString()
        R.defaultLocale = locale
    }
}