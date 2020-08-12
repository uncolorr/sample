package ru.icames.store.domain.use_case

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import ru.icames.store.domain.repository.AuthRepository
import ru.icames.store.domain.repository.QueryRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.AppUpdateInfo
import ru.icames.store.domain.model.Error
import ru.icames.store.util.Logger

import javax.inject.Inject

class QueryUseCase @Inject constructor(private val queryRepository: QueryRepository, authRepository: AuthRepository): AuthUseCase(authRepository){

    fun queryAppUpdates(
        type: String,
        q: String,
        offset: Int,
        sort: String,
        onSuccess: ((t: List<AppUpdateInfo>) -> Unit),
        onError: ((t: Throwable) -> Unit)){
        disposeLast()
        lastDisposable = queryRepository.queryAppUpdates(type, q, offset, sort)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, { throwable ->
                    if(throwable is HttpException){
                        val errorBody: String = throwable.response()?.errorBody()?.string() ?: ""
                        Logger.logEvent(Logger.Event.START_PROCESS_FAILURE, errorBody)
                        if(errorBody.isEmpty()){
                            onError(throwable)
                            return@subscribe
                        }
                        val mapper = jacksonObjectMapper()
                        val error = mapper.readValue(errorBody, Error::class.java)
                        if(error != null) {
                            when (error.StatusCode) {
                                401 -> {
                                    refreshToken(
                                            onSuccess = {
                                                AppSettings.save(AppSettings.KEY_AUTH_DATA, it)
                                                queryAppUpdates(type, q, offset, sort, onSuccess, onError)
                                            },
                                            onError = {
                                                onError(throwable)
                                            })
                                }
                                else -> onError(throwable)
                            }
                        }
                        else {
                            onError(throwable)
                        }
                    }
                    else {
                        onError(throwable)
                    }
                })
        lastDisposable?.let {
            compositeDisposable.add(it)
        }
    }
}


