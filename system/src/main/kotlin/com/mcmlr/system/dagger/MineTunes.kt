package com.mcmlr.system.dagger

import com.mcmlr.system.products.minetunes.MineTunesApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MineTunesScope

@MineTunesScope
@Subcomponent(modules = [MineTunesAppModule::class])
interface MineTunesAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: MineTunesApp): Builder
        fun build(): MineTunesAppComponent
    }

    fun inject(app: MineTunesApp)
}

@Module
class MineTunesAppModule
