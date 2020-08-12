package ru.icames.store.presentation.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Base class for ViewModels
 *
 */

abstract class BaseViewModel: ViewModel() {

    var isLoading = MutableLiveData<Boolean>()

    var isError = MutableLiveData<Boolean>()

}