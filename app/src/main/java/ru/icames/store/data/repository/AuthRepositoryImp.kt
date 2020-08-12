package ru.icames.store.data.repository

import io.reactivex.Single
import ru.icames.store.application.AppSettings
import ru.icames.store.data.ApiService
import ru.icames.store.domain.model.AuthData
import ru.icames.store.domain.repository.AuthRepository


class AuthRepositoryImp constructor(private val apiService: ApiService): AuthRepository {

    override fun loginWith(): Single<AuthData> {
        val token = AppSettings.get<String>(AppSettings.KEY_APP_TOKEN) as String
        val login = AppSettings.get<String>(AppSettings.KEY_LOGIN) as String
        val password = AppSettings.get<String>(AppSettings.KEY_PASSWORD) as String
        return apiService.loginWith(token, login, password)
    }

    override fun checkAuthToken(): Single<AuthData> {
        return apiService.checkAuthToken(AppSettings.get<AuthData>(AppSettings.KEY_AUTH_DATA)!!.authToken)
    }
}