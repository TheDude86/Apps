package com.mcmlr.system.dagger

import com.mcmlr.system.products.heads.HeadsApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class HeadsScope

@HeadsScope
@Subcomponent(modules = [HeadsAppModule::class])
interface HeadsAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: HeadsApp): Builder
        fun build(): HeadsAppComponent
    }

    fun inject(app: HeadsApp)
}

@Module
class HeadsAppModule