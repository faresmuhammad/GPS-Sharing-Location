package com.fares.gpssharinglocation.di.modules.profiles

import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.di.annotations.ViewModelKey
import com.fares.gpssharinglocation.ui.activities.profiles.ProfilesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ProfilesViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfilesViewModel::class)
    abstract fun bindProfilesViewModel(viewModel: ProfilesViewModel): ViewModel
}