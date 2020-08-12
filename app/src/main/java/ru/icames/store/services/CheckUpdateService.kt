package ru.icames.store.services

import android.R
import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import ru.icames.store.application.App
import ru.icames.store.presentation.app_update.AppUpdateManager
import javax.inject.Inject


class CheckUpdateService @Inject constructor() : IntentService(SERVICE_NAME) {

    companion object {

        private const val NOTIFICATION_ID = 4502

        private const val NOTIFICATION_CHANNEL_ID = "ru.icames.terminal.update"

        private const val SERVICE_NAME = "CheckUpdateService"

        fun newIntent(context: Context): Intent {
            return Intent(context, CheckUpdateService::class.java)
        }
    }

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    init {
        //DaggerApiComponent.create().inject(this)
        App.getAppComponent().inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        App.log("onCreate CheckUpdateService")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else  {
            startForeground(NOTIFICATION_ID, generateNotification())
        }
    }

    @SuppressLint("NewApi")
    private fun startMyOwnForeground() {
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, SERVICE_NAME, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        startForeground(NOTIFICATION_ID, generateNotification())
    }

    private fun generateNotification(): Notification{
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
       notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_menu_upload)
                .setContentTitle("Проверка обновлений...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
        }
        return notificationBuilder.build()
    }

    override fun onHandleIntent(intent: Intent?) {
        App.log("onHandleIntent check updates")
        appUpdateManager.checkUpdates()
    }


}