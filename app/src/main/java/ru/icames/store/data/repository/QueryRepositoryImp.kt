package ru.icames.store.data.repository


import io.reactivex.Single
import ru.icames.store.data.ApiConfig
import ru.icames.store.data.ApiService
import ru.icames.store.domain.model.AppUpdateInfo
import ru.icames.store.domain.repository.QueryRepository

class QueryRepositoryImp constructor(val apiService: ApiService): QueryRepository {

    override fun queryAppUpdates(type: String, q: String, offset: Int, sort: String): Single<List<AppUpdateInfo>> {
        return apiService.queryAppUpdates(ApiConfig.getTokenHeaders(), type, q, 1, offset, sort)
    }
}