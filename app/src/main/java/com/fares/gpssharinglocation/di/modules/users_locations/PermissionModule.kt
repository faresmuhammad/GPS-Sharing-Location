package com.fares.gpssharinglocation.di.modules.users_locations

import com.fares.gpssharinglocation.data.AppPreferences
import com.fares.gpssharinglocation.utils.PermissionsManager
import dagger.Module
import dagger.Provides

@Module
class PermissionModule {

    @Provides
    fun providePermissionManager(preferences: AppPreferences) =
        PermissionsManager(preferences)
}