package ru.icames.store.data

import okhttp3.Interceptor
import okhttp3.Response

class ProgressDownloadInterceptor constructor(private val progressListener: ProgressListener): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        chain.request().header("File-Downloading") ?: return originalResponse
        return originalResponse.newBuilder()
            .body(ProgressResponseBody(originalResponse.body(), progressListener))
            .build()
    }
}