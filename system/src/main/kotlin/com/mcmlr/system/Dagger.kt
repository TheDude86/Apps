package com.mcmlr.system

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.CursorRepository
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

@Module(subcomponents = [DefaultAppComponent::class])
class SubcomponentModule




@EnvironmentScope
@Singleton
@Component(modules = [DefaultEnvironmentModule::class, SubcomponentModule::class])
interface DefaultEnvironmentComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun environment(environment: DefaultEnvironment): Builder
        fun build(): DefaultEnvironmentComponent
    }

    fun subcomponent(): DefaultAppComponent.Builder

    fun inject(environment: DefaultEnvironment)
}

@Module
class DefaultEnvironmentModule {
    @EnvironmentScope
    @Provides
    fun resources(environment: DefaultEnvironment): Resources = environment.resources
}






@AppScope
@Subcomponent(modules = [DefaultAppModule::class])
interface DefaultAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: DefaultApp): Builder
        fun build(): DefaultAppComponent
    }

    fun inject(app: DefaultApp)
}

@Module
class DefaultAppModule {
    @AppScope
    @Provides
    fun player(app: DefaultApp): Player = app.player

    @AppScope
    @Provides
    fun origin(player: Player): Location {
        val o = player.eyeLocation.clone()
        o.pitch = 0f //TODO: Fix pitch translation issue & remove

        val direction = o.direction.normalize()
        return o.add(direction.multiply(0.15))
    }

    @AppScope
    @Provides
    fun cursorRepository(): CursorRepository = SystemCursorRepository()


}
