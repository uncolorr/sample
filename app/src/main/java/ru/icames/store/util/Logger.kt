package ru.icames.store.util

import ru.icames.store.domain.model.DataArray
import ru.icames.store.domain.use_case.StartProcessUseCase
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.data.ApiConfig
import ru.icames.store.domain.use_case.LogReporterUseCase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class Logger {

    companion object {

        private const val FILENAME = "log.txt"

        fun logEvent(event: Event, message: String = ""){
            val path = App.getContext().getExternalFilesDir(null)!!.absolutePath + File.separator + FILENAME
            val file = File(path)
            if(!file.exists()){
                file.createNewFile()
            }

            val builder = StringBuilder()
            val date = getCurrentDateTime()
            val dateInString = date.toString("yyyy/MM/dd HH:mm:ss")
            val log = builder.append(dateInString)
                    .append(": ")
                    .append(event.description)
                    .append(": ")
                    .append(message)
                    .append("\r\n")
                    .toString()
            file.appendText(log)
        }

        private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        private fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }

        fun getLogFilePath(): String{
            return App.getContext().getExternalFilesDir(null)!!.absolutePath + File.separator + FILENAME
        }
    }

    @Inject
    lateinit var logReporterUseCase: LogReporterUseCase

    @Inject
    lateinit var startProcessUseCase: StartProcessUseCase

    init {
        App.getAppComponent().inject(this)
        //DaggerApiComponent.create().inject(this)
    }

    fun uploadReport(onSuccess: ((t: String) -> Unit),
                     onError: ((t: Throwable) -> Unit)): Boolean {
        val body = ApiConfig.prepareLogFileBody() ?: return false
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy_MM_dd_HH_mm_ss")
        val name = dateInString + "_" + FILENAME
        logReporterUseCase.sendReport(name, body, onSuccess, onError)
        return true
    }

    fun sendReportToEmail(fileUid: String,
                          onSuccess: ((t: DataArray) -> Unit),
                          onError: ((t: Throwable) -> Unit)){
        val baseParams = hashMapOf<String, String>()
        val contextParams = hashMapOf<String, String>()
        baseParams[Fields.PackingTasks.PROCESS_TOKEN] = ProcessToken.SEND_REPORT_TO_EMAIL.value
        baseParams[Fields.PackingTasks.PROCESS_NAME] = "SendLog"
        contextParams[Fields.PackingTasks.FILE_UID] = fileUid
        contextParams[Fields.PackingTasks.COMPANY] = AppSettings.get<String>(AppSettings.KEY_COMPANY) ?: ""
        val body = Converter.convertDataToProcessRequestBody(baseParams, contextParams)
        startProcessUseCase.startProcess(body, onSuccess, onError)
    }

    enum class Event(val description: String) {
        AUTH_APP_SUCCESS("УСПЕШНАЯ АВТОРИЗАЦИЯ ПРИЛОЖЕНИЯ"),
        AUTH_APP_FAILURE("ОШИБКА АВТОРИЗАЦИИ ПРИЛОЖЕНИЯ"),
        START_PROCESS_SUCCESS("УСПЕШНЫЙ ЗАПУСК ПРОЦЕССА"),
        START_PROCESS_FAILURE("ОШИБКА ЗАПУСКА ПРОЦЕССА"),
        UPDATE_TOKEN_SUCCESS("УСПЕШНОЕ ОБНОВЛЕНИЕ ТОКЕНА СЕССИИ"),
        UPDATE_TOKEN_FAILURE("ОШИБКА ОБНОВЛЕНИЯ ТОКЕНА СЕССИИ"),
        QUERY_SUCCESS("УСПЕШНЫЙ ЗАПРОС ДАННЫХ"),
        QUERY_FAILURE("ОШИБКА ПРИ ЗАПРОСЕ ДАННЫХ"),
        CATCH_VALUE("ОТСКАНИРОВАНО ЗНАЧЕНИЕ"),
        SEND_LOG_REPORT_FAILURE("ОШИБКА ПРИ ОТПРАВКЕ ОТЧЕТА РАБОТЫ"),
        UNCAUGHT_EXCEPTION("ПОЙМАНО ИСКЛЮЧЕНИЕ")

    }


}