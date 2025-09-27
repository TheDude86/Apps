package com.mcmlr.system.dagger

import com.mcmlr.system.products.homes.HomesApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class HomesScope

@HomesScope
@Subcomponent(modules = [HomesAppModule::class])
interface HomesAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: HomesApp): Builder
        fun build(): HomesAppComponent
    }

    fun inject(app: HomesApp)
}

@Module
class HomesAppModule
