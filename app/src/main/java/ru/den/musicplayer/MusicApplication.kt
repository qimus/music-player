package ru.den.musicplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.den.musicplayer.di.appModule
import ru.den.musicplayer.di.databaseModule
import ru.den.musicplayer.di.viewModelModule

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MusicApplication)
            modules(appModule, databaseModule, viewModelModule)
        }
    }
}
