package ru.icames.store.presentation.app_update

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.util.SingleLiveEvent
import ru.icames.store.util.TypeUid
import ru.icames.store.BuildConfig
import ru.icames.store.domain.model.AppUpdateInfo
import ru.icames.store.services.CheckUpdateService
import ru.icames.store.domain.use_case.QueryUseCase
import java.io.File
import javax.inject.Inject

class AppUpdateManager {

    companion object {

        private const val CHECK_UPDATE_PERIOD = 12 * 60 * 60 * 1000
        private const val SHOW_UPDATE_PERIOD = 24 * 60 * 60 * 1000

        const val UPDATE_FILENAME = "update.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "application/vnd.android.package-archive"

        fun checkUpdatesInBackground() {
            if (!canCheckUpdate()) {
                return
            }
            val lastUpdateCheckTime = AppSettings.get<Long>(AppSettings.KEY_UPDATE_CHECK_TIME) ?: 0
            if (System.currentTimeMillis() - lastUpdateCheckTime >= CHECK_UPDATE_PERIOD) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    App.getContext()
                        .startForegroundService(CheckUpdateService.newIntent(App.getContext()))
                } else {
                    App.getContext().startService(CheckUpdateService.newIntent(App.getContext()))
                }
            }
        }

        private fun canCheckUpdate(): Boolean {
            return AppSettings.get<Boolean>(AppSettings.KEY_CAN_CHECK_UPDATES) ?: false
        }
    }

    var noUpdatesMessage = SingleLiveEvent<Void>()
    var lastUpdateAlreadyMessage = SingleLiveEvent<Void>()
    var showUpdateInfo = SingleLiveEvent<Void>()
    var isChecking = MutableLiveData<Boolean>()
    var isError = MutableLiveData<Boolean>()

    @Inject
    lateinit var queryUseCase: QueryUseCase

    init {
        App.getAppComponent().inject(this)
        //DaggerApiComponent.create().inject(this)
    }

    fun checkUpdates() {
        App.log("checkUpdates")
        isChecking.postValue(true)
        queryUseCase.queryAppUpdates(
            TypeUid.APP_UPDATES.uid, "", 0, "CreationDate DESC",
            onSuccess = {
                isChecking.value = false
                val updates = it
                if (updates.isEmpty()) {
                    noUpdatesMessage.call()
                    return@queryAppUpdates
                }
                if (updates.isNotEmpty()) {
                    val lastUpdate = updates[0]
                    if (lastUpdate.versionCode == BuildConfig.VERSION_CODE) {
                        lastUpdateAlreadyMessage.call()
                        return@queryAppUpdates
                    }
                    if (lastUpdate.versionCode > BuildConfig.VERSION_CODE) {
                        if (!AppSettings.isInit()) {
                            AppSettings.init(App.getContext())
                        }
                        setUpdateInfo(lastUpdate)
                        updateCheckTime()
                        showUpdateInfo.call()
                    }
                }
            },
            onError = {
                isChecking.value = false
                isError.value = true
            })
    }

    private fun setUpdateInfo(appUpdateInfo: AppUpdateInfo) {
        AppSettings.save(AppSettings.KEY_UPDATE, appUpdateInfo)
    }

    fun showUpdateInfoUI(fragmentManager: FragmentManager) {
        val bottomSheetUpdateFragment = BottomSheetUpdateFragment()
        bottomSheetUpdateFragment.show(fragmentManager, "updateFragment")
    }

    fun getUpdateInfo(): AppUpdateInfo? {
        return AppSettings.get(AppSettings.KEY_UPDATE)
    }

    fun startUpdateApp(destination: String) {
        val context = App.getContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                File(destination)
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = contentUri
            context.startActivity(install)
        } else {
            val uri = Uri.parse(FILE_BASE_PATH + destination)
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.setDataAndType(
                uri,
                APP_INSTALL_PATH
            )
            context.startActivity(install)
        }
    }

    fun updateShowTime() {
        AppSettings.save(AppSettings.KEY_UPDATE_SHOW_TIME, System.currentTimeMillis())
    }

    private fun updateCheckTime() {
        AppSettings.save(AppSettings.KEY_UPDATE_CHECK_TIME, System.currentTimeMillis())
    }

    fun canShowUpdate(): Boolean {
        if (!canCheckUpdate()) {
            return false
        }
        if (!AppSettings.contains(AppSettings.KEY_UPDATE)) {
            return false
        }
        val lastUpdateShowTime = AppSettings.get<Long>(AppSettings.KEY_UPDATE_SHOW_TIME) ?: 0
        if (System.currentTimeMillis() - lastUpdateShowTime >= SHOW_UPDATE_PERIOD) {
            return true
        }
        return false
    }


}