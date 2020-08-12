package ru.icames.store.domain.repository

import io.reactivex.Single
import ru.icames.store.domain.model.AuthData

interface AuthRepository {

    /**
     * Method for app auth.
     * @return AuthData with AuthToken and SessionToken
     * */
    fun loginWith(): Single<AuthData>

    /**
     * Method for update AuthData. AuthToken lives only 15 minutes and needed to update.
     * @return AuthData with AuthToken and SessionToken
     * */
    fun checkAuthToken(): Single<AuthData>
}
