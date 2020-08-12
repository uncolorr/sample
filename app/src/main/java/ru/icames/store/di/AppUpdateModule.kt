package ru.icames.store.di

import dagger.Module
import dagger.Provides
import ru.icames.store.presentation.app_update.AppUpdateManager
import javax.inject.Singleton

@Module
class AppUpdateModule {

    @Provides
    @Singleton
    fun providesAppUpdateManager(): AppUpdateManager {
        return AppUpdateManager()
    }
}