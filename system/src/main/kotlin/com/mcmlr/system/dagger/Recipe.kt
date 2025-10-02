package com.mcmlr.system.dagger

import com.mcmlr.system.products.recipe.RecipeApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RecipeScope

@RecipeScope
@Subcomponent(modules = [RecipeAppModule::class])
interface RecipeAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: RecipeApp): Builder
        fun build(): RecipeAppComponent
    }

    fun inject(app: RecipeApp)
}

@Module
class RecipeAppModule