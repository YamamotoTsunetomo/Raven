package com.tsunetomo.raven

import android.app.Application
import com.tsunetomo.raven.di.appModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    private val appContextModule = module {
        single { applicationContext }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appContextModule, *appModules)
        }
    }
}