package ru.icames.store.presentation.start

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_start.*
import ru.icames.store.R
import ru.icames.store.annotations.LaunchActivityResult
import ru.icames.store.application.AppSettings
import ru.icames.store.presentation.code_reader.QrCodeReaderActivity
import ru.icames.store.presentation.code_reader.config.AuthEmployeeQrCodeReaderConfig
import ru.icames.store.presentation.main.MainActivity
import ru.icames.store.presentation.settings.SettingsActivity
import ru.icames.store.util.LoadingDialog
import ru.icames.store.util.Logger
import ru.icames.store.util.MessageReporter
import ru.icames.store.util.SingleLiveEvent

class StartActivity : AppCompatActivity() {

    private lateinit var loadingDialog: AlertDialog

    private var isLoading = MutableLiveData<Boolean>()

    private var isError = SingleLiveEvent<Void>()

    private var reportSent = SingleLiveEvent<Void>()


    companion object {
        fun getInstance(context: Context): Intent {
            return Intent(context, StartActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        loadingDialog = LoadingDialog.newInstanceWithoutCancelable(this)
        buttonStart.setOnClickListener {
            if (AppSettings.contains(AppSettings.KEY_IS_EMPLOYEE_AUTH)) {
                startActivity(MainActivity.getInstance(this))
            } else {
                val config = AuthEmployeeQrCodeReaderConfig()
                startActivity(QrCodeReaderActivity.getInstance(this, config))
            }
        }

        buttonSettings.setOnClickListener {
            startActivity(SettingsActivity.getInstance(this, true))
        }

        buttonSendReport.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    LaunchActivityResult.permissions
                )
            } else {
                uploadReport()
            }
        }

        isLoading.observe(this, Observer {
            if(it) loadingDialog.show() else loadingDialog.hide()
        })

        isError.observe(this, Observer {
            MessageReporter.showMessage(this, "Ошибка", "Ошибка при отправке отчета работы.")
        })

        reportSent.observe(this, Observer {
            MessageReporter.showMessage(this, "Сообщение", "Отчет о работе успешно отправлен.")
        })
    }

    private fun uploadReport(){
        isLoading.value = true
        val sent = Logger().uploadReport(
            onSuccess = {
                sendToEmail(it)
            },
            onError = {
                isLoading.value = false
                isError.call()
            })
        if(!sent){
            MessageReporter.showMessage(this, "Ошибка", "Файл с отчетом о работе не найден.")
            isLoading.value = false
        }
    }

    private fun sendToEmail(fileUid: String){
        Logger().sendReportToEmail(fileUid,
            onSuccess = {
                isLoading.value = false
                reportSent.call()
            },
            onError = {
                isLoading.value = false
                isError.call()
            })
    }
}
