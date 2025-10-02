package com.mcmlr.system.dagger

import com.mcmlr.system.products.preferences.PreferencesApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PreferencesScope

@PreferencesScope
@Subcomponent(modules = [PreferencesAppModule::class])
interface PreferencesAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: PreferencesApp): Builder
        fun build(): PreferencesAppComponent
    }

    fun inject(app: PreferencesApp)
}

@Module
class PreferencesAppModule