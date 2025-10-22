package com.mcmlr.system.dagger

import com.mcmlr.blocks.api.Resources
import com.mcmlr.system.SystemApp
import com.mcmlr.system.SystemEnvironment
import com.mcmlr.system.products.homes.HomesApp
import com.mcmlr.system.products.pong.PongApp
import com.mcmlr.system.products.settings.AdminApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvironmentScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@Module(subcomponents = [SystemAppComponent::class])
class SubcomponentModule

@Module(
    subcomponents = [
        HomesAppComponent::class,
        AdminAppComponent::class,
        AnnouncementsAppComponent::class,
        WarpsAppComponent::class,
        TeleportAppComponent::class,
        MarketAppComponent::class,
        PreferencesAppComponent::class,
        SpawnAppComponent::class,
        WorkbenchesAppComponent::class,
        RecipeAppComponent::class,
        KitsAppComponent::class,
        TutorialAppComponent::class,
        CheatsAppComponent::class,
        PongAppComponent::class,
        YAMLAppComponent::class,
    ]
)
class HomeSubcomponentModule




@EnvironmentScope
@Singleton
@Component(modules = [SystemEnvironmentModule::class, SubcomponentModule::class])
interface SystemEnvironmentComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun environment(environment: SystemEnvironment): Builder
        fun build(): SystemEnvironmentComponent
    }

    fun subcomponent(): SystemAppComponent.Builder

    fun inject(environment: SystemEnvironment)
}

@Module
class SystemEnvironmentModule {
    @EnvironmentScope
    @Provides
    fun resources(environment: SystemEnvironment): Resources = environment.resources
}






@AppScope
@Subcomponent(modules = [SystemAppModule::class, HomeSubcomponentModule::class])
interface SystemAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: SystemApp): Builder
        fun build(): SystemAppComponent
    }

    fun homeSubcomponent(): HomesAppComponent.Builder

    fun adminSubcomponent(): AdminAppComponent.Builder

    fun announcementsSubcomponent(): AnnouncementsAppComponent.Builder

    fun warpsSubcomponent(): WarpsAppComponent.Builder

    fun teleportSubcomponent(): TeleportAppComponent.Builder

    fun marketSubcomponent(): MarketAppComponent.Builder

    fun preferencesSubcomponent(): PreferencesAppComponent.Builder

    fun spawnSubcomponent(): SpawnAppComponent.Builder

    fun workbenchesSubcomponent(): WorkbenchesAppComponent.Builder

    fun recipeSubcomponent(): RecipeAppComponent.Builder

    fun kitsSubcomponent(): KitsAppComponent.Builder

    fun tutorialSubcomponent(): TutorialAppComponent.Builder

    fun cheatsSubcomponent(): CheatsAppComponent.Builder

    fun pongSubcomponent(): PongAppComponent.Builder

    fun yamlSubcomponent(): YAMLAppComponent.Builder

    fun inject(app: SystemApp)
}

@Module
class SystemAppModule {
    @AppScope
    @Provides
    fun player(app: SystemApp): Player = app.player

    @AppScope
    @Provides
    fun origin(player: Player): Location {
        val o = player.eyeLocation.clone()
        o.pitch = 0f

        val direction = o.direction.normalize()
        return o.add(direction.multiply(0.15))
    }
}


