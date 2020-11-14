package com.fares.gpssharinglocation

import com.fares.gpssharinglocation.di.component.DaggerAppComponent
import com.fares.gpssharinglocation.model.User
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber


class GPSSharingApp : DaggerApplication() {

    var user: User? = null
        get() = field
        set(value) {
            field = value
        }


//        fun isUserNull() = user == null
    val isUserNull: Boolean
        get() = user == null

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.factory()
            .create(this)
        appComponent.inject(this)

        return appComponent
    }

    val appComponent = DaggerAppComponent.factory().create(this)
}