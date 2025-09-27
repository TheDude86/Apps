package com.mcmlr.system.products.data

import com.mcmlr.blocks.api.Resources
import com.mcmlr.system.dagger.EnvironmentScope
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import javax.inject.Inject

@EnvironmentScope
class VaultRepository @Inject constructor(
    resources: Resources,
) {

    val economy: Economy?
    val permissions: Permission?
    val chat: Chat?

    init {
        if (resources.server().pluginManager.getPlugin("Vault") != null) {
            val economyProvider = resources.server().servicesManager.getRegistration(Economy::class.java)
            val permissionProvider = resources.server().servicesManager.getRegistration(Permission::class.java)
            val chatProvider = resources.server().servicesManager.getRegistration(Chat::class.java)
            economy = economyProvider?.provider
            permissions = permissionProvider?.provider
            chat = chatProvider?.provider
        } else {
            economy = null
            permissions = null
            chat = null
        }
    }

}