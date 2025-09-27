package com.mcmlr.system.dagger

import com.mcmlr.system.products.warps.WarpsApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WarpsScope

@WarpsScope
@Subcomponent(modules = [WarpsAppModule::class])
interface WarpsAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: WarpsApp): Builder
        fun build(): WarpsAppComponent
    }

    fun inject(app: WarpsApp)
}

@Module
class WarpsAppModule