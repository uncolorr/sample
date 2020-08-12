package ru.icames.store.domain.use_case

import ru.icames.store.domain.repository.AuthRepository
import ru.icames.store.domain.use_case.base.UseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.icames.store.domain.model.AuthData
import javax.inject.Inject

open class AuthUseCase @Inject constructor(open val authRepository: AuthRepository): UseCase() {

    fun loginWith(
        onSuccess: ((t: AuthData) -> Unit),
        onError: ((t: Throwable) -> Unit)
    ) {
        disposeLast()
        lastDisposable = authRepository.loginWith()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
        lastDisposable?.let {
            compositeDisposable.add(it)
        }
    }


    fun refreshToken(
            onSuccess: ((t: AuthData) -> Unit),
            onError: ((t: Throwable) -> Unit)
    ) {
        disposeLast()
        lastDisposable = authRepository.checkAuthToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
        lastDisposable?.let {
            compositeDisposable.add(it)
        }
    }

}