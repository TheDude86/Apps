package com.mcmlr.system.dagger

import com.mcmlr.system.products.spawn.SpawnApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SpawnScope

@SpawnScope
@Subcomponent(modules = [SpawnAppModule::class])
interface SpawnAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: SpawnApp): Builder
        fun build(): SpawnAppComponent
    }

    fun inject(app: SpawnApp)
}

@Module
class SpawnAppModule