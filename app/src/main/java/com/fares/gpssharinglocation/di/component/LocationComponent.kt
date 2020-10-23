package com.fares.gpssharinglocation.di.component

import com.fares.gpssharinglocation.di.annotations.LocationScope
import com.fares.gpssharinglocation.services.LocationService
import dagger.Subcomponent

@LocationScope
@Subcomponent(
    modules = [
//        FirebaseModule::class
    ]
)
interface LocationComponent {

    fun inject(service: LocationService)

    @Subcomponent.Factory
    interface Factory {
        fun create(): LocationComponent
    }
}