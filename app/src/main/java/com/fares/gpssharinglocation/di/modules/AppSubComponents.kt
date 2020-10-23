package com.fares.gpssharinglocation.di.modules

import com.fares.gpssharinglocation.di.component.AuthComponent
import com.fares.gpssharinglocation.di.component.LocationComponent
import dagger.Module

@Module(
    subcomponents = [
    AuthComponent::class,
    LocationComponent::class
    ]
)
class AppSubComponents