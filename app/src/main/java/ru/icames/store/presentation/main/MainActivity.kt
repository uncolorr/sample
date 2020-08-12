package ru.icames.store.presentation.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_main.*
import ru.icames.store.R
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.presentation.app_update.AppUpdateManager
import ru.icames.store.presentation.start.StartActivity
import ru.icames.store.presentation.code_reader.AccountingMode
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val navigator = MainActivityNavigator(this)

    companion object {

        fun getInstance(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    init {
        App.getAppComponent().inject(this)
        //DaggerApiComponent.create().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fillEmployeeInfo(cardViewEmployee, textViewEmployeeName)

        buttonLogout.setOnClickListener {
            logout()
        }

        buttonTakeObject.setOnClickListener {
            navigator.openTakeObject()
        }

        buttonPutObject.setOnClickListener {
            navigator.openPutObject()
        }
    }

    override fun onResume() {
        super.onResume()
        val canShowUpdate = appUpdateManager.canShowUpdate()
        if(canShowUpdate) {
            appUpdateManager.showUpdateInfoUI(supportFragmentManager)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillEmployeeInfo(cardView: CardView, textViewName: TextView) {
        if(AppSettings.contains(AppSettings.KEY_OPERATOR)){
            val info = AppSettings.get<String>(AppSettings.KEY_OPERATOR)
            val items = info?.split(",")
            if(items!!.size != 5){
                cardView.visibility = View.GONE
                return
            }
            textViewName.text = "${items[2]} ${items[3]} ${items[4]}"
        }
        else {
            cardView.visibility = View.GONE
        }
    }

    private fun logout(){
        AppSettings.remove(AppSettings.KEY_OPERATOR)
        AppSettings.remove(AppSettings.KEY_IS_EMPLOYEE_AUTH)
        startActivity(StartActivity.getInstance(this))
        finishAffinity()

    }
}