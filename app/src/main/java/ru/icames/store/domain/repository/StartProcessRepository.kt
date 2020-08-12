package ru.icames.store.domain.repository

import io.reactivex.Single
import ru.icames.store.domain.model.DataArray

interface StartProcessRepository {
    /**
     * Method for start process on server. For example, method uses for send scanning data.
     * @param requestBody data format with sending values
     * */
    fun startProcess(requestBody: DataArray): Single<DataArray>
}