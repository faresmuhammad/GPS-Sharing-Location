package com.fares.gpssharinglocation.di.modules

import android.app.Application
import android.content.Context
import com.fares.gpssharinglocation.data.AppPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreferencesModule {

    @Singleton
    @Provides
    fun provideContext(app: Application): Context = app

    @Singleton
    @Provides
    fun provideAppPreferences(context: Context) =
        AppPreferences(context)

}