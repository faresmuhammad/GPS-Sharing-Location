package com.fares.gpssharinglocation.di.modules.users_locations

import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.di.annotations.ViewModelKey
import com.fares.gpssharinglocation.ui.activities.users_locations.UsersLocationsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UsersLocationsViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(UsersLocationsViewModel::class)
    abstract fun bindUsersLocationsViewModel(viewModel: UsersLocationsViewModel): ViewModel
}