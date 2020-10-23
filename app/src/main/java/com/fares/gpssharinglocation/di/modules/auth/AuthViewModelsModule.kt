package com.fares.gpssharinglocation.di.modules.auth

import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.di.annotations.ViewModelKey
import com.fares.gpssharinglocation.ui.activities.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelsModule{

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel
}