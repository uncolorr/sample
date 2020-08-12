package ru.icames.store.presentation.auth_app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.HttpException
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.use_case.AuthUseCase
import ru.icames.store.util.Logger
import ru.icames.store.util.SingleLiveEvent
import javax.inject.Inject

class AuthAppViewModel : ViewModel() {

    var isLoading = MutableLiveData<Boolean>()

    var auth = SingleLiveEvent<Void>()

    var isError = MutableLiveData<Boolean>()


    @Inject
    lateinit var authUseCase: AuthUseCase


    init {
        App.getAppComponent().inject(this)
        //DaggerApiComponent.create().inject(this)
    }


    fun authApp() {
        isError.value = false
        isLoading.value = true
        authUseCase.loginWith(
            onSuccess = {
                AppSettings.save(AppSettings.KEY_AUTH_DATA, it)
                AppSettings.save(AppSettings.KEY_IS_APP_AUTH, true)
                auth.call()
                Logger.logEvent(Logger.Event.AUTH_APP_SUCCESS)

            },
            onError = {
                isLoading.value = false
                isError.value = true
                var errorBody = ""
                if (it is HttpException) {
                    App.log(it.response()!!.message())
                   // App.log(it.response().errorBody()!!.string())
                    errorBody = it.response()?.errorBody()?.string() ?: ""
                }
                Logger.logEvent(Logger.Event.AUTH_APP_FAILURE, errorBody)

            })
    }

    override fun onCleared() {
        super.onCleared()
        authUseCase.dispose()
    }

}