package ru.icames.store.presentation.auth_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_auth_app.*
import ru.icames.store.R
import ru.icames.store.presentation.start.StartActivity

class AuthAppActivity : AppCompatActivity() {

    companion object {

        fun getInstance(context: Context): Intent {
            return Intent(context, AuthAppActivity::class.java)
        }
    }

    private lateinit var viewModel: AuthAppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_app)
        viewModel = ViewModelProviders.of(this).get(AuthAppViewModel::class.java)

        buttonRepeat.setOnClickListener {
            viewModel.authApp()
        }
        observeViewModel()

        viewModel.authApp()
    }

    private fun observeViewModel(){
        viewModel.auth.observe(this, Observer {
            startActivity(StartActivity.getInstance(this))
            finishAffinity()
        })

        viewModel.isError.observe(this, Observer {
            buttonRepeat.visibility = if(it) View.VISIBLE else View.INVISIBLE
            if(it){
                textViewStatus.text = getString(R.string.auth_app_label_error)
                progressBar.visibility = View.INVISIBLE
            }
        })

        viewModel.isLoading.observe(this, Observer {
            if(it) {
                textViewStatus.text = getString(R.string.auth_app_label)
                buttonRepeat.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
        })
    }
}