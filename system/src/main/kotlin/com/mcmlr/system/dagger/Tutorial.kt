package com.mcmlr.system.dagger

import com.mcmlr.system.products.info.TutorialApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class TutorialScope

@TutorialScope
@Subcomponent(modules = [TutorialAppModule::class])
interface TutorialAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: TutorialApp): Builder
        fun build(): TutorialAppComponent
    }

    fun inject(app: TutorialApp)
}

@Module
class TutorialAppModule