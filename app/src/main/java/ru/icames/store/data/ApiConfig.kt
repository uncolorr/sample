package ru.icames.store.data

import ru.icames.store.domain.model.AuthData
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.icames.store.util.Logger
import ru.icames.store.application.AppSettings
import java.io.File

/**
 * Singleton class with template data for API requests
 * */

object ApiConfig {

    /**
     * Return AuthToken and SessionToken for API requests
     * @return map with needed tokens
     * */
    fun getTokenHeaders(): Map<String, String?>{
        val map = HashMap<String, String?>()
        map["AuthToken"] = AppSettings.get<AuthData>(AppSettings.KEY_AUTH_DATA)?.authToken ?: ""
        map["SessionToken"] = AppSettings.get<AuthData>(AppSettings.KEY_AUTH_DATA)?.sessionToken ?: ""
        return map
    }

    /**
     * Return preparing logs file for sending to support
     * @see ru.icames.terminal.util.Logger
     * @return request body with logs file
     * */
    fun prepareLogFileBody(): MultipartBody.Part? {
        val file = File(Logger.getLogFilePath())
        if(!file.exists()){
            return null
        }
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("text/plain"), file)
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }
}