package ru.icames.store.domain.use_case

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.icames.store.domain.repository.AuthRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import retrofit2.HttpException
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.Error
import ru.icames.store.domain.repository.LogReporterRepository
import ru.icames.store.util.Logger
import javax.inject.Inject

class LogReporterUseCase @Inject constructor(private val logReporterRepository: LogReporterRepository,
                                             override val authRepository: AuthRepository
):  AuthUseCase(authRepository) {
    fun sendReport(filename: String,
                   body: MultipartBody.Part,
                   onSuccess: ((t: String) -> Unit),
                   onError: ((t: Throwable) -> Unit)){
        disposeLast()
        lastDisposable = logReporterRepository.sendReport(filename, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, { throwable ->
                    if (throwable is HttpException) {
                        val errorBody: String = throwable.response()?.errorBody()?.string() ?: ""
                        Logger.logEvent(Logger.Event.SEND_LOG_REPORT_FAILURE, errorBody)
                        if(errorBody.isEmpty()){
                            onError(throwable)
                            return@subscribe
                        }
                        val mapper = jacksonObjectMapper()
                        val error = mapper.readValue(errorBody, Error::class.java)

                        if (error != null) {
                            when (error.StatusCode) {
                                401 -> {
                                    refreshToken(
                                            onSuccess = {
                                                AppSettings.save(AppSettings.KEY_AUTH_DATA, it)
                                                Logger.logEvent(Logger.Event.UPDATE_TOKEN_SUCCESS)
                                                sendReport(filename, body, onSuccess, onError)
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