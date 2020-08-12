package ru.icames.store.domain.repository

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface DownloadRepository {
    fun download(uid: String): Single<Response<ResponseBody>>
}