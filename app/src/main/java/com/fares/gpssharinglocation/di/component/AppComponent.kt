package com.fares.gpssharinglocation.di.component

import android.app.Application
import com.fares.gpssharinglocation.GPSSharingApp
import com.fares.gpssharinglocation.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBuilderModule::class,
        ViewModelFactoryModule::class,
        AppSubComponents::class,
        PreferencesModule::class,
        FirebaseModule::class
    ]
)
interface AppComponent : AndroidInjector<GPSSharingApp> {

    fun inject(application: Application)
//    fun inject(service: LocationService)

    val authComponentFactory: AuthComponent.Factory

    val locationComponentFactory: LocationComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }


}