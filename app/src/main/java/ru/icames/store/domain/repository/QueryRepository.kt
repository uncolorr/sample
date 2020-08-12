package ru.icames.store.domain.repository

import io.reactivex.Single
import ru.icames.store.domain.model.AppUpdateInfo


interface QueryRepository {

    /**
     * Method for execute query. For example, method uses for get details or tasks.

     * @param type TypeUid. It is a unique key for entity
     * @param q EQL-query for ELMA
     * @param offset offset for query request
     * @param sort sort for list in response
     * */
    fun queryAppUpdates(type: String, q: String, offset: Int, sort: String): Single<List<AppUpdateInfo>>
}