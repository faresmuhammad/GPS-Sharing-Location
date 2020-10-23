package com.fares.gpssharinglocation.di.modules

import com.fares.gpssharinglocation.di.annotations.AuthScope
import com.fares.gpssharinglocation.di.modules.auth.AuthViewModelsModule
import com.fares.gpssharinglocation.di.modules.profiles.ProfilesViewModelsModule
import com.fares.gpssharinglocation.di.modules.users_locations.PermissionModule
import com.fares.gpssharinglocation.di.modules.users_locations.UsersLocationsViewModelsModule
import com.fares.gpssharinglocation.ui.activities.auth.AuthActivity
import com.fares.gpssharinglocation.ui.activities.profiles.ProfilesActivity
import com.fares.gpssharinglocation.ui.activities.users_locations.UsersLocationsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @AuthScope
    @JvmSuppressWildcards(true)
    @ContributesAndroidInjector(
        modules = [
//            ViewModelFactoryModule::class,
            AuthViewModelsModule::class
        ]
    )
    abstract fun bindAuthActivity(): AuthActivity

    @ContributesAndroidInjector(
        modules = [
//            ViewModelFactoryModule::class,
            ProfilesViewModelsModule::class,
            PermissionModule::class
        ]
    )
    abstract fun bindProfilesActivity(): ProfilesActivity


    @ContributesAndroidInjector(
        modules = [
//            ViewModelFactoryModule::class,
            UsersLocationsViewModelsModule::class,
            PermissionModule::class
        ]
    )
    abstract fun bindUsersLocationsActivity(): UsersLocationsActivity
}