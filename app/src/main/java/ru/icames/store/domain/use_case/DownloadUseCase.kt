package ru.icames.store.domain.use_case

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.icames.store.domain.repository.AuthRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import ru.icames.store.domain.model.Error
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.repository.DownloadRepository
import ru.icames.store.util.Logger
import javax.inject.Inject

class DownloadUseCase @Inject constructor(private val downloadRepository: DownloadRepository,
                                          override val authRepository: AuthRepository
) : AuthUseCase(authRepository) {

    fun downloadFile(uid: String,
                     onSuccess: ((t: Response<ResponseBody>) -> Unit),
                     onError: ((t: Throwable) -> Unit)) {
        lastDisposable = downloadRepository.download(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.isSuccessful) {
                        onSuccess(response)
                        return@subscribe
                    }

                    if (response.code() == 400) {
                        val errorBody = response.errorBody()?.string() ?: ""
                        if (errorBody.isEmpty()) {
                            onError(Throwable())
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
                                                downloadFile(uid, onSuccess, onError)
                                            },
                                            onError = {
                                                onError(Throwable(errorBody))
                                                Logger.logEvent(Logger.Event.UPDATE_TOKEN_FAILURE, errorBody)
                                            })
                                }
                                else -> {
                                    onError(Throwable(errorBody))
                                }
                            }
                        } else {
                            onError(Throwable(errorBody))
                        }
                    }
                },
                        { throwable ->
                            onError(throwable)
                        })
    }

}