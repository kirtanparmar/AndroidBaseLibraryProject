package com.kirtan.mylibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApiViewModel<ApiResponseType> : ViewModel() {
    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()

    fun getResponseData(): LiveData<ApiResponseType> = responseData

    fun setResponseData(responseData: ApiResponseType) {
        this.responseData.value = responseData
    }
}