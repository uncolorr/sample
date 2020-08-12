package ru.icames.store.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import ru.icames.store.data.ApiService
import ru.icames.store.data.HostSelectionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.icames.store.data.ProgressListener
import ru.icames.store.domain.repository.*
import ru.icames.store.util.DownloadManager
import ru.icames.store.data.ProgressDownloadInterceptor
import ru.icames.store.data.repository.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    companion object {
        private const val BASE_URL = "http://example.com/API/REST/"
    }

    @Provides
    @Singleton
    fun providesRetrofit(
            gsonConverterFactory: GsonConverterFactory,
            rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
            okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(progressDownloadInterceptor: ProgressDownloadInterceptor): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .addNetworkInterceptor(progressDownloadInterceptor)
                .addInterceptor(HostSelectionInterceptor())
        return client.build()
    }

    @Provides
    @Singleton
    fun providesProgressListener(): ProgressListener {
        return DownloadManager.progressListener
    }

    @Provides
    @Singleton
    fun providesProgressDownloadInterceptor(progressListener: ProgressListener): ProgressDownloadInterceptor {
        return ProgressDownloadInterceptor(progressListener)
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

    @Singleton
    @Provides
    fun provideService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        return AuthRepositoryImp(apiService)
    }

    @Singleton
    @Provides
    fun provideQueryRepository(apiService: ApiService): QueryRepository {
        return QueryRepositoryImp(apiService)
    }

    @Singleton
    @Provides
    fun provideStartProcessRepository(apiService: ApiService): StartProcessRepository {
        return StartProcessRepositoryImp(apiService)
    }

    @Singleton
    @Provides
    fun provideLogReporterRepository(apiService: ApiService): LogReporterRepository {
        return LogReporterRepositoryImp(apiService)
    }

    @Singleton
    @Provides
    fun provideDownloadRepository(apiService: ApiService): DownloadRepository {
        return DownloadRepositoryImp(apiService)
    }
}