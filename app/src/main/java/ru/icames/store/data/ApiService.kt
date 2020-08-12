package ru.icames.store.data

import ru.icames.store.domain.model.AuthData
import ru.icames.store.domain.model.DataArray
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import ru.icames.store.domain.model.AppUpdateInfo

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("API secured :)")
    fun loginWith(@Header("ApplicationToken") applicationToken: String,
                  @Query("username") username: String,
                  @Body password: String): Single<AuthData>

    @Headers("Content-Type: application/json")
    @GET("API secured :)")
    fun checkAuthToken(@Query("token") authToken: String): Single<AuthData>


    @Headers("Content-Type: application/json")
    @POST("API secured :)")
    fun startProcess(@HeaderMap headers: Map<String, String?>, @Body requestBody: DataArray): Single<DataArray>


    @Headers("Content-Type: application/json", "WebData-Version: 2.0")
    @GET("API secured :)")
    fun queryAppUpdates(@HeaderMap headers: Map<String, String?>,
                        @Query("type") type: String,
                        @Query("q") q: String,
                        @Query("limit") limit: Int,
                        @Query("offset") offset: Int,
                        @Query("sort") sort: String): Single<List<AppUpdateInfo>>

    @Multipart
    @POST("API secured :)")
    fun sendReport(@HeaderMap headers: Map<String, String?>, @Part body: MultipartBody.Part): Single<String>

    @Streaming
    @Headers("File-Downloading: true")
    @GET("API secured :)")
    fun downloadFile(@HeaderMap headers: Map<String, String?>, @Query("uid") uid: String): Single<Response<ResponseBody>>
}