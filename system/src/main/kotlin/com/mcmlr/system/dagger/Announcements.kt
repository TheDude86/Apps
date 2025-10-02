package com.mcmlr.system.dagger

import com.mcmlr.system.products.announcements.AnnouncementsApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AnnouncementsScope

@AdminScope
@Subcomponent(modules = [AnnouncementsAppModule::class])
interface AnnouncementsAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: AnnouncementsApp): Builder
        fun build(): AnnouncementsAppComponent
    }

    fun inject(app: AnnouncementsApp)
}

@Module
class AnnouncementsAppModule