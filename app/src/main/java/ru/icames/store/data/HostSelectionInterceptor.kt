package ru.icames.store.data

import ru.icames.store.application.AppSettings
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Http Interceptor for replacing holder in base url from NetWorkModule to dynamic url from SettingsActivity
 * @see ru.icames.store.presentation.settings.SettingsActivity
 * @see ru.icames.store.di.NetworkModule.BASE_URL
 * */

class HostSelectionInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val currentUrl = request.url().toString()
        val domain = AppSettings.get<String>(AppSettings.KEY_DOMAIN)
        val port = AppSettings.get<String>(AppSettings.KEY_PORT)
        val newUrl = currentUrl.replace("example.com",  "$domain:$port")
        val httpUrl: HttpUrl? = HttpUrl.parse(newUrl)
        val newRequest = request.newBuilder()
                    .url(httpUrl!!)
                    .build()

        return chain.proceed(newRequest)
    }
}
