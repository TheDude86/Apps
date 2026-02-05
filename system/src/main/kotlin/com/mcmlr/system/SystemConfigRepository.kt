package com.mcmlr.system

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.AppConfigModel
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.entity.Player
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

    private val activeBillboardsFlow: MutableSharedFlow<List<Pair<Player, BillboardModel>>> = MutableSharedFlow()

    init {
        loadModel("", "config", AppConfigModel())
    }

    fun getBillboardsStream(): Flow<List<Pair<Player, BillboardModel>>> = activeBillboardsFlow

    fun updateBillboardsStream(data: List<Pair<Player, BillboardModel>>) = activeBillboardsFlow.emitBackground(data)

    fun editBillboard(billboard: BillboardModel) = save {
        var index = -1
        for (i in 0..<model.billboards.billboards.size) {
            if (model.billboards.billboards[i].id == billboard.id) {
                index = i
                break
            }
        }

        if (index > -1) model.billboards.billboards[index] = billboard
    }

    fun addBillboard(billboard: BillboardModel) = save {
        model.billboards.billboards.add(billboard)
    }

    fun removeBillboard(billboard: BillboardModel) = save {
        model.billboards.billboards.removeIf { it.id == billboard.id }
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
