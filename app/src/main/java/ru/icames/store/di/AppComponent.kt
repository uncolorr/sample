package ru.icames.store.di

import dagger.Component
import ru.icames.store.presentation.code_reader.QrCodeReaderViewModel
import ru.icames.store.presentation.main.MainActivity
import ru.icames.store.application.App
import ru.icames.store.presentation.app_update.AppUpdateManager
import ru.icames.store.presentation.app_update.BottomSheetUpdateFragment
import ru.icames.store.presentation.auth_app.AuthAppViewModel
import ru.icames.store.presentation.base.StartProcessViewModel
import ru.icames.store.presentation.settings.SettingsActivity
import ru.icames.store.services.CheckUpdateService
import ru.icames.store.util.DownloadManager
import ru.icames.store.util.Logger
import javax.inject.Singleton


@Singleton
@Component(modules = [NetworkModule::class, AppUpdateModule::class])
interface AppComponent {


    fun inject(qrCodeReaderViewModel: QrCodeReaderViewModel)

    fun inject(startProcessViewModel: StartProcessViewModel)

    fun inject(authAppViewModel: AuthAppViewModel)

    fun inject(logger: Logger)

    fun inject(checkUpdateService: CheckUpdateService)

    fun inject(downloadManager: DownloadManager)

    fun inject(appUpdateManager: AppUpdateManager)

    fun inject(app: App)

    fun inject(bottomSheetUpdateFragment: BottomSheetUpdateFragment)

    fun inject(settingsActivity: SettingsActivity)

    fun inject(mainActivity: MainActivity)

}