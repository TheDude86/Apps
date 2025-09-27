package com.mcmlr.system.dagger

import com.mcmlr.system.products.cheats.CheatsApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class CheatsScope

@CheatsScope
@Subcomponent(modules = [CheatsAppModule::class])
interface CheatsAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: CheatsApp): Builder
        fun build(): CheatsAppComponent
    }

    fun inject(app: CheatsApp)
}

@Module
class CheatsAppModule