package com.example.tiendatematisada251630

import android.app.Application
import com.example.tiendatematisada251630.data.AppContainer
import com.example.tiendatematisada251630.data.DefaultAppContainer

class StoreApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
