package com.mcmlr.system.dagger

import com.mcmlr.system.products.market.MarketApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MarketScope

@MarketScope
@Subcomponent(modules = [MarketAppModule::class])
interface MarketAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: MarketApp): Builder
        fun build(): MarketAppComponent
    }

    fun inject(app: MarketApp)
}

@Module
class MarketAppModule