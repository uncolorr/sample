package ru.icames.store.data.repository

import io.reactivex.Single
import ru.icames.store.data.ApiConfig
import ru.icames.store.data.ApiService
import ru.icames.store.domain.model.DataArray
import ru.icames.store.domain.repository.StartProcessRepository

class StartProcessRepositoryImp constructor(val apiService: ApiService): StartProcessRepository {

    override fun startProcess(requestBody: DataArray): Single<DataArray> {
        return apiService.startProcess(ApiConfig.getTokenHeaders(), requestBody)
    }
}