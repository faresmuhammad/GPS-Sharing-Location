package com.fares.gpssharinglocation.di.component

import com.fares.gpssharinglocation.di.annotations.AuthScope
import dagger.Subcomponent

@AuthScope
@Subcomponent(
    modules = [
//        AuthModule::class
    ]
)
interface AuthComponent {


    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }


}