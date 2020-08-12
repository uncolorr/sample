package ru.icames.store.application

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDexApplication
import ru.icames.store.di.AppComponent
import ru.icames.store.di.DaggerAppComponent
import ru.icames.store.presentation.app_update.AppUpdateManager
import ru.icames.store.util.Logger
import java.io.PrintWriter
import java.io.StringWriter


class App : MultiDexApplication() {

    companion object {

        /**
         * Print logs in Logcat
         * @param message text for print in Logcat
         * */
        fun log(message: String) {
            if (message.isEmpty()) {
                Log.i("fg", "{Log is empty}")
            }
            Log.i("fg", message)
        }

        private lateinit var instance: App

        /**
         * Access to application context
         * @return application context
         * */
        fun getContext(): Context {
            return instance.applicationContext
        }

        private val component = DaggerAppComponent.builder().build()

        fun getAppComponent(): AppComponent = component


    }

    private val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
        initExceptionHandler()
        instance = this

    }


    /**
     * Set custom exception handler for logging uncaught exceptions in log file
     * */
    private fun initExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            catchError(e)
            defaultUncaughtHandler?.uncaughtException(t, e)
        }
    }

    /**
     * Catch throwable and write it in log file
     * @param e catching throwable
     * */
    private fun catchError(e: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val stackTrace = sw.toString()
        Logger.logEvent(Logger.Event.UNCAUGHT_EXCEPTION, stackTrace)
    }
}