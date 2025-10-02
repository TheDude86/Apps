package com.mcmlr.system.dagger

import com.mcmlr.system.products.kits.KitsApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class KitsScope

@KitsScope
@Subcomponent(modules = [KitsAppModule::class])
interface KitsAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: KitsApp): Builder
        fun build(): KitsAppComponent
    }

    fun inject(app: KitsApp)
}

@Module
class KitsAppModule