package com.mcmlr.system.dagger

import com.mcmlr.system.products.workbenches.WorkbenchesApp
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkbenchesScope

@WorkbenchesScope
@Subcomponent(modules = [WorkbenchesAppModule::class])
interface WorkbenchesAppComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun app(app: WorkbenchesApp): Builder
        fun build(): WorkbenchesAppComponent
    }

    fun inject(app: WorkbenchesApp)
}

@Module
class WorkbenchesAppModule