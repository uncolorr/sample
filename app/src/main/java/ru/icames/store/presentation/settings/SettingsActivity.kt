package ru.icames.store.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_settings.*
import ru.icames.store.BuildConfig
import ru.icames.store.R
import ru.icames.store.annotations.LaunchActivityResult
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.presentation.app_update.AppUpdateManager
import ru.icames.store.presentation.auth_app.AuthAppActivity
import ru.icames.store.presentation.start.StartActivity
import ru.icames.store.util.*
import ru.icames.store.util.ConfigReader.Keys.Companion.KEY_APPLICATION_TOKEN
import ru.icames.store.util.ConfigReader.Keys.Companion.KEY_DOMAIN
import ru.icames.store.util.ConfigReader.Keys.Companion.KEY_LOGIN
import ru.icames.store.util.ConfigReader.Keys.Companion.KEY_PASSWORD
import ru.icames.store.util.ConfigReader.Keys.Companion.KEY_PORT
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    companion object {

        private const val ARG_FROM_MENU = "from_menu"

        fun getInstance(context: Context, fromMenu: Boolean = false): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(ARG_FROM_MENU, fromMenu)
            return intent
        }
    }

    private val codecs = Codec.values()

    private val scannerModels = DeviceType.values()

    private var appToken = MutableLiveData<String>()

    private lateinit var loading: AlertDialog



    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    init {
        App.getAppComponent().inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fromMenu = intent.getBooleanExtra(ARG_FROM_MENU, false)

        if (!fromMenu) {
            if (AppSettings.contains(AppSettings.KEY_IS_APP_AUTH)) {
                startActivity(StartActivity.getInstance(this))
                finish()
                return
            }
        }
        setContentView(R.layout.activity_settings)
        loading = LoadingDialog.newInstanceWithoutCancelable(this)
        appUpdateManager = AppUpdateManager()
        observeViewModel()
        textViewBuildVersionName.text = BuildConfig.VERSION_NAME

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                LaunchActivityResult.permissions
            )
        }

        setupCodecsSpinner()
        setupDeviceTypesSpinner()

        checkDataContains()

        buttonAuth.setOnClickListener {
            if (checkFillDataFields()) {
                AppSettings.save(AppSettings.KEY_DOMAIN, editTextDomain.text.toString())
                AppSettings.save(AppSettings.KEY_PORT, editTextPort.text.toString())
                AppSettings.save(AppSettings.KEY_LOGIN, editTextLogin.text.toString())
                AppSettings.save(AppSettings.KEY_PASSWORD, editTextPassword.text.toString())
                AppSettings.save(AppSettings.KEY_APP_TOKEN, appToken.value)
                AppSettings.save(AppSettings.KEY_CODEC, spinnerCodecs.selectedItem as Codec)
                AppSettings.save(AppSettings.KEY_DEVICE_TYPE, spinnerDeviceTypes.selectedItem as DeviceType)
                AppSettings.save(AppSettings.KEY_COMPANY, editTextCompanyName.text.toString())
                AppSettings.save(AppSettings.KEY_CAN_CHECK_UPDATES, checkboxCheckUpdatesAutomatically.isChecked)
                startActivity(AuthAppActivity.getInstance(this))
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_LONG).show()
            }
        }

        registerForContextMenu(buttonLoadFromConfigFile)
        buttonLoadFromConfigFile.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    LaunchActivityResult.permissions
                )
            } else {
                openContextMenu(buttonLoadFromConfigFile)
            }
        }

        buttonCheckUpdates.setOnClickListener {
            appUpdateManager.checkUpdates()
        }
    }

    private fun observeViewModel() {
        appToken.observe(this, Observer {
            if (it.isEmpty()) {
                buttonAuth.isEnabled = false
                imageViewTokenState.setImageResource(R.drawable.ic_attention)
                textViewTokenState.text = "Токен авторизации приложения не найден"
            } else {
                buttonAuth.isEnabled = true
                imageViewTokenState.setImageResource(R.drawable.ic_check_circle)
                textViewTokenState.text = "Токен авторизации приложения найден"
            }
        })

        appUpdateManager.showUpdateInfo.observe(this, Observer {
            appUpdateManager.showUpdateInfoUI(supportFragmentManager)
        })

        appUpdateManager.noUpdatesMessage.observe(this, Observer {
            MessageReporter.showMessage(this, "Сообщение",
                "Доступных обновлений для приложения не обнаружено")
        })

        appUpdateManager.lastUpdateAlreadyMessage.observe(this, Observer {
            MessageReporter.showMessage(this, "Сообщение",
                "Актуальная версия приложения уже установлена")
        })

        appUpdateManager.isChecking.observe(this, Observer {
            if(it) {
                loading.show()
            } else {
                loading.hide()
            }
        })

        appUpdateManager.isError.observe(this, Observer {
            if(it) {
                MessageReporter.showMessage(this, "Ошибка",
                    "Неизвестная ошибка")
            }
        })
    }

    private fun loadFromConfigFile(view: View, path: String){
        val configReader = ConfigReader(this)
        val params = configReader.getConfigData(path)
        if (params == null) {
            val snackBar = Snackbar.make(view, "Ошибка считывания данных из файла", Snackbar.LENGTH_LONG)
            snackBar.show()
            App.log("Ошибка считывания данных")
            return
        }

        editTextDomain.setText(params[KEY_DOMAIN])
        editTextPort.setText(params[KEY_PORT])
        editTextLogin.setText(params[KEY_LOGIN])
        editTextPassword.setText(params[KEY_PASSWORD])
        appToken.value = params[KEY_APPLICATION_TOKEN]
        val snackBar = Snackbar.make(view, "Данные авторизации успешно загружены", Snackbar.LENGTH_LONG)
        snackBar.show()
    }


    private fun checkDataContains() {
        if (AppSettings.contains(AppSettings.KEY_DOMAIN)) {
            editTextDomain.setText(AppSettings.get<String>(AppSettings.KEY_DOMAIN))
        }
        if (AppSettings.contains(AppSettings.KEY_PORT)) {
            editTextPort.setText(AppSettings.get<String>(AppSettings.KEY_PORT))
        }
        if (AppSettings.contains(AppSettings.KEY_LOGIN)) {
            editTextLogin.setText(AppSettings.get<String>(AppSettings.KEY_LOGIN))
        }
        if (AppSettings.contains(AppSettings.KEY_PASSWORD)) {
            editTextPassword.setText(AppSettings.get<String>(AppSettings.KEY_PASSWORD))
        }
        if (AppSettings.contains(AppSettings.KEY_COMPANY)) {
            editTextCompanyName.setText(AppSettings.get<String>(AppSettings.KEY_COMPANY))
        }

        if (AppSettings.contains(AppSettings.KEY_APP_TOKEN)) {
            appToken.value = AppSettings.get<String>(AppSettings.KEY_APP_TOKEN)
        } else {
            appToken.value = ""
        }

        if (AppSettings.contains(AppSettings.KEY_CODEC)) {
            when (AppSettings.get<Codec>(AppSettings.KEY_CODEC)) {
                Codec.UTF_8 -> {
                    spinnerCodecs.setSelection(0)
                }
                Codec.WINDOWS_1251 -> {
                    spinnerCodecs.setSelection(1)
                }
            }
        }

        if (AppSettings.contains(AppSettings.KEY_DEVICE_TYPE)) {
            when (AppSettings.get<DeviceType>(AppSettings.KEY_DEVICE_TYPE)) {
                DeviceType.SMART_LITE -> {
                    spinnerDeviceTypes.setSelection(0)
                }
                DeviceType.SMART_DROID -> {
                    spinnerDeviceTypes.setSelection(1)
                }
                DeviceType.SMARTPHONE -> {
                    spinnerDeviceTypes.setSelection(2)
                }
            }
        }

        checkboxCheckUpdatesAutomatically.isChecked = AppSettings.get<Boolean>(AppSettings.KEY_CAN_CHECK_UPDATES) ?: false
    }

    private fun checkFillDataFields(): Boolean {
        val fields = arrayListOf(
            editTextCompanyName,
            editTextDomain,
            editTextPort,
            editTextLogin,
            editTextPassword)

        return fields.none {
            it.text!!.isEmpty()
        }
    }

    private fun setupCodecsSpinner() {
        val adapter = ArrayAdapter<Codec>(this, android.R.layout.simple_spinner_item, codecs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCodecs.adapter = adapter
        spinnerCodecs.setSelection(0)
    }

    private fun setupDeviceTypesSpinner() {
        val adapter = ArrayAdapter<DeviceType>(this, android.R.layout.simple_spinner_item, scannerModels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceTypes.adapter = adapter
        spinnerDeviceTypes.setSelection(0)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_read_settings, menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.readFromDefaultFile -> {
                loadFromConfigFile(buttonLoadFromConfigFile, ConfigReader.getDefaultPath())
                return true
            }
            R.id.readFromFileExplorer -> {
                ChooserDialog(this)
                    .withFilter(false, false, ConfigReader.CONFIG_EXTENSION)
                    .withChosenListener { _, file ->
                        loadFromConfigFile(buttonLoadFromConfigFile, file.absolutePath)
                    }
                    .build()
                    .show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
