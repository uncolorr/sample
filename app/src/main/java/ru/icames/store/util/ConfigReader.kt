package ru.icames.store.util

import android.content.Context
import ru.icames.store.application.App
import java.io.BufferedReader
import java.io.File

class  ConfigReader(val context: Context) {

    companion object {

        private const val FINAL_PARAMS_COUNT = 5

        private const val FILENAME = "config.txt"

        private val keys = arrayOf(
            Keys.KEY_APPLICATION_TOKEN,
            Keys.KEY_DOMAIN,
            Keys.KEY_LOGIN,
            Keys.KEY_PASSWORD,
            Keys.KEY_PORT
        )

        const val CONFIG_EXTENSION = "txt"

        fun getDefaultPath(): String{
            return App.getContext().getExternalFilesDir(null)!!.absolutePath + File.separator + FILENAME
        }
    }

    fun getConfigData(path: String): Map<String, String>? {
        val data = getDataFromConfigFile(path) ?: return null
        return parseData(data) ?: return null
    }

    private fun getDataFromConfigFile(path: String): String? {
        // val path = context.getExternalFilesDir(null)!!.absolutePath + File.separator + FILENAME
        val file = File(path)
        App.log("file path: " + file.absoluteFile)
        if (!file.exists()) {
            App.log("file not exists")
            return null
        }

        val bufferedReader: BufferedReader = file.bufferedReader()
        val result = bufferedReader.use { it.readText() }
        if (result.isEmpty()) {
            return null
        }
        App.log("file data: $result")
        return result
    }

    private fun checkAllDataContains(params: Map<String, String>): Boolean {
        if (params.size != FINAL_PARAMS_COUNT) {
            App.log("Wrong params count")
            return false
        }

        val mapKeys = params.keys

        for (key in keys) {
            if (!mapKeys.contains(key)) {
                return false
            }
        }
        return true
    }

    private fun parseData(data: String): Map<String, String>? {
        val params = HashMap<String, String>()
        var dataWithoutLn = data.replace("\n", "")
        dataWithoutLn = dataWithoutLn.replace(" ", "")
        dataWithoutLn = dataWithoutLn.replace("\r", "")
        val strings: ArrayList<String> = dataWithoutLn.split(";") as ArrayList<String>
        strings.removeAt(strings.lastIndex)
        App.log("lines count: " + strings.size)

        for (string in strings) {
            val pair = string.split("=")
            if (pair.size != 2) {
                App.log("Wrong pair format")
                return null
            }

            if (!keys.contains(pair[0])) {
                App.log("Wrong parameter: '${pair[0]}' = '${pair[1]}'")
                return null
            }

            if (pair[1].isEmpty()) {
                App.log("Empty parameter")
                return null
            }
            params[pair[0]] = pair[1]
            App.log("params added: ${pair[0]} = ${pair[1]}")
        }

        if (!checkAllDataContains(params)) {
            App.log("No needed params exists")
            return null
        }

        return params
    }

    class Keys {

        companion object {
            const val KEY_DOMAIN = "DOMAIN"
            const val KEY_PORT = "PORT"
            const val KEY_LOGIN = "LOGIN"
            const val KEY_PASSWORD = "PASSWORD"
            const val KEY_APPLICATION_TOKEN = "APPLICATION_TOKEN"
        }

    }
}
