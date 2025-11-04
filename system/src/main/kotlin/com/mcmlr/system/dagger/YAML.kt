package com.mcmlr.system.dagger

import com.mcmlr.system.products.yaml.YAMLApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class YAMLScope

@YAMLScope
@Subcomponent(modules = [YAMLAppModule::class])
interface YAMLAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: YAMLApp): Builder
        fun build(): YAMLAppComponent
    }

    fun inject(app: YAMLApp)
}

@Module
class YAMLAppModule