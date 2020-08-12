package ru.icames.store.domain.use_case

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.icames.store.domain.repository.AuthRepository
import ru.icames.store.domain.repository.StartProcessRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.DataArray
import ru.icames.store.domain.model.Error
import ru.icames.store.util.Logger
import javax.inject.Inject


class StartProcessUseCase @Inject constructor(private val startProcessRepository: StartProcessRepository,
                                              override val authRepository: AuthRepository
) : AuthUseCase(authRepository) {

    fun startProcess(
        requestBody: DataArray,
        onSuccess: ((t: DataArray) -> Unit),
        onError: ((t: Throwable) -> Unit)) {
        disposeLast()
        lastDisposable = startProcessRepository.startProcess(requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, { throwable ->
                    if (throwable is HttpException) {
                        val errorBody: String = throwable.response()?.errorBody()?.string() ?: ""
                        Logger.logEvent(Logger.Event.START_PROCESS_FAILURE, errorBody)
                        if(errorBody.isEmpty()){
                            onError(throwable)
                            return@subscribe
                        }
                        val mapper = jacksonObjectMapper()
                        val error = mapper.readValue<Error>(errorBody, Error::class.java)

                        if (error != null) {

                            when (error.StatusCode) {
                                401 -> {

                                    refreshToken(
                                            onSuccess = {
                                                AppSettings.save(AppSettings.KEY_AUTH_DATA, it)
                                                Logger.logEvent(Logger.Event.UPDATE_TOKEN_SUCCESS)
                                                startProcess(requestBody, onSuccess, onError)
                                            },
                                            onError = {
                                                onError(throwable)
                                                Logger.logEvent(Logger.Event.UPDATE_TOKEN_FAILURE, errorBody)
                                            })
                                }
                                else -> onError(throwable)
                            }
                        } else {
                            onError(throwable)
                        }
                    } else {
                        onError(throwable)
                    }
                })
        lastDisposable?.let {
            compositeDisposable.add(it)
        }
    }
}