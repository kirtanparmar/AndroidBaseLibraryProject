package com.kirtan.mylibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApiCallingViewModel<ApiResponseType> : ViewModel() {
    var status: MutableLiveData<ApiStatus> = MutableLiveData(ApiStatus.INIT)
    var dataFed: Boolean = false
    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()

    fun getResponseData(): LiveData<ApiResponseType> = responseData

    fun setResponseData(responseData: ApiResponseType) {
        dataFed = false
        this.responseData.value = responseData
    }

    enum class ApiStatus { INIT, COMPLETE, LOADING }
}