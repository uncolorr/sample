package ru.icames.store.presentation.base

import androidx.lifecycle.MutableLiveData
import ru.icames.store.domain.use_case.StartProcessUseCase
import ru.icames.store.application.App
import ru.icames.store.domain.model.DataArray
import ru.icames.store.util.SingleLiveEvent
import javax.inject.Inject

open class StartProcessViewModel: BaseViewModel() {

    var endWork = SingleLiveEvent<Void>()

    var isLoadingProcess = MutableLiveData<Boolean>()

    @Inject
    lateinit var startProcessUseCase: StartProcessUseCase

    init {
        App.getAppComponent().inject(this)
        //DaggerApiComponent.create().inject(this)
    }

    fun startProcess(requestBody: DataArray) {
        isError.value = false
        isLoadingProcess.value = true
        startProcessUseCase.startProcess(
                requestBody,
                onSuccess = {
                    isLoadingProcess.value = false
                    endWork.call()
                },
                onError = {
                    isLoadingProcess.value = false
                    isError.value = true
                })
    }
}