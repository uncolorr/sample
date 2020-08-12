package ru.icames.store.application

import android.content.Context
import com.orhanobut.hawk.Hawk

/**
 * Singleton class for saving app preferences
 * */

object AppSettings {

    const val KEY_COMPANY = "company"

    const val KEY_AUTH_DATA = "auth_data"

    const val KEY_OPERATOR = "operator"

    const val KEY_DOMAIN = "domain"

    const val KEY_PORT = "port"

    const val KEY_LOGIN = "login"

    const val KEY_PASSWORD = "password"

    const val KEY_IS_APP_AUTH = "is_app_auth"

    const val KEY_IS_EMPLOYEE_AUTH = "is_employee_auth"

    const val KEY_APP_TOKEN = "application_token"

    const val KEY_CODEC = "codec"

    const val KEY_DEVICE_TYPE = "scanner_model"

    const val KEY_CAN_CHECK_UPDATES = "can_check_updates"

    const val KEY_UPDATE = "update"

    const val KEY_UPDATE_SHOW_TIME = "update_show_time"

    const val KEY_UPDATE_CHECK_TIME = "update_check_time"



    /**
     * Init Hawk framework for saving app preferences
     * Method should be called in Application class
     * @param context application Context
     * */
    fun init(context: Context){
        App.log("init hawk")
        Hawk.init(context).build()
    }

    fun isInit() : Boolean{
        return Hawk.isBuilt()
    }

    /**
     * Save app preferences
     * @param key name of preference
     * @param value saving value
     * */
    fun <Type> save(key: String, value: Type) {
        Hawk.put(key, value)
    }

    /**
     * Return app preference by key
     * @param key name of preference
     * @return preference by key. If preference not exists returns null.
     * */
    fun <Type> get(key: String): Type? {
        if(!Hawk.contains(key)){
            return null
        }
        return Hawk.get(key)
    }

    /**
     * Remove app preference by key
     * @param key name of preference
     * */
    fun remove(key: String){
        Hawk.delete(key)
    }

    /**
     * Check is app preference exists
     * @param key name of preference
     * @return is app preference exists.
     * */
    fun contains(key: String): Boolean {
        return Hawk.contains(key)
    }
}