package com.mcmlr.pluginengine

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.system.SystemCursorRepository
import com.mcmlr.system.SystemInputRepository
import com.mcmlr.system.SystemPlayerChatRepository
import com.mcmlr.system.SystemResources
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import javax.inject.Singleton

@Singleton
@Component(modules = [ManagerModule::class])
interface ManagerComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: Engine): Builder
        fun build(): ManagerComponent
    }

    fun inject(plugin: Engine)

}

@Module
object ManagerModule {

    @Provides
    fun plugin(plugin: Engine): JavaPlugin = Engine.instance

    @Provides
    fun server(plugin: Engine): Server = Engine.instance.server

    @Provides
    fun dataFolder(plugin: Engine): File = Engine.instance.dataFolder

    @Provides
    fun economy(server: Server): Economy? = server.servicesManager.getRegistration(Economy::class.java)?.provider

    @Provides
    fun permission(server: Server): Permission? = server.servicesManager.getRegistration(Permission::class.java)?.provider


    @Singleton
    @Provides
    fun cursorRepository(): CursorRepository = SystemCursorRepository()

    @Singleton
    @Provides
    fun playerChatRepository(): PlayerChatRepository = SystemPlayerChatRepository()

    @Singleton
    @Provides
    fun inputRepository(): InputRepository = SystemInputRepository()

    @Singleton
    @Provides
    fun resources(server: Server, plugin: JavaPlugin, dataFolder: File): Resources = SystemResources(server, dataFolder, plugin)
}
