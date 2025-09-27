package com.mcmlr.system.dagger

import com.mcmlr.system.products.teleport.TeleportApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class TeleportScope

@TeleportScope
@Subcomponent(modules = [TeleportAppModule::class])
interface TeleportAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: TeleportApp): Builder
        fun build(): TeleportAppComponent
    }

    fun inject(app: TeleportApp)
}

@Module
class TeleportAppModule