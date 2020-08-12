package ru.icames.store.data.repository

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import ru.icames.store.data.ApiConfig
import ru.icames.store.data.ApiService
import ru.icames.store.domain.repository.DownloadRepository

class DownloadRepositoryImp constructor(val apiService: ApiService): DownloadRepository {
    override fun download(uid: String): Single<Response<ResponseBody>> {
       return apiService.downloadFile(ApiConfig.getTokenHeaders(), uid)
    }


}