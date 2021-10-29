package com.kirtan.mylibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApiViewModel<ApiResponseType> : ViewModel() {
    var status: MutableLiveData<ApiStatus> = MutableLiveData(ApiStatus.INITIATE)
    var dataFed: Boolean = false
    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()

    fun getResponseData(): LiveData<ApiResponseType> = responseData

    fun setResponseData(responseData: ApiResponseType) {
        dataFed = false
        this.responseData.value = responseData
    }

    enum class ApiStatus { INITIATE, LOADED, LOADING }
}