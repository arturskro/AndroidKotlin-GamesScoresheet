package pl.arturs.android.gamescoresheet

import android.app.Application

class GSSApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicjalizacja repozytorium
        AppRepository.initialize(this)
    }
}