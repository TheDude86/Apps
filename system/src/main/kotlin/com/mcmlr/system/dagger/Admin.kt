package com.mcmlr.system.dagger

import com.mcmlr.system.products.settings.AdminApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AdminScope

@AdminScope
@Subcomponent(modules = [AdminAppModule::class])
interface AdminAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: AdminApp): Builder
        fun build(): AdminAppComponent
    }

    fun inject(app: AdminApp)
}

@Module
class AdminAppModule