package ru.icames.store.data.repository

import io.reactivex.Single
import okhttp3.MultipartBody
import ru.icames.store.data.ApiConfig
import ru.icames.store.data.ApiService
import ru.icames.store.domain.repository.LogReporterRepository

class LogReporterRepositoryImp constructor(val apiService: ApiService): LogReporterRepository {
    override fun sendReport(filename: String, body: MultipartBody.Part): Single<String> {
        val headers:HashMap<String, String?> = ApiConfig.getTokenHeaders() as HashMap<String, String?>
        headers["FileName"] = filename
        return apiService.sendReport(headers, body)
    }
}