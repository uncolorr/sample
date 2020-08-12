package ru.icames.store.domain.repository

import io.reactivex.Single
import okhttp3.MultipartBody

interface LogReporterRepository {

    /**
     * Method for send report to support.
     * @param filename name that will see in support
     * @param body file in MultipartBody.Part format
     * */
    fun sendReport(filename: String, body: MultipartBody.Part): Single<String>
}