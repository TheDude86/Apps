package com.mcmlr.system.dagger

import com.mcmlr.system.products.pong.PongApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PongScope

@PongScope
@Subcomponent(modules = [PongAppModule::class])
interface PongAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: PongApp): Builder
        fun build(): PongAppComponent
    }

    fun inject(app: PongApp)
}

@Module
class PongAppModule