package com.kirtan.baseLibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Response
import timber.log.Timber

open class ApiCallingViewModel<ApiResponseType> : ViewModel() {
    var apiStatus: MutableLiveData<ApiStatus> = MutableLiveData(ApiStatus.INIT)
    var dataFed: Boolean = false

    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()
    fun getResponseData(): LiveData<ApiResponseType> = responseData
    fun setResponseData(responseData: ApiResponseType) {
        dataFed = false
        this.responseData.value = responseData
    }

    enum class ApiStatus { INIT, COMPLETE, LOADING }

    private val apiResponse: MutableLiveData<Response<ApiResponseType>?> =
        object : MutableLiveData<Response<ApiResponseType>?>() {
            override fun setValue(value: Response<ApiResponseType>?) {
                super.setValue(value)
                apiStatus.value = ApiStatus.COMPLETE
                Timber.d("$value")
            }
        }

    fun getApiResponse(): LiveData<Response<ApiResponseType>?> = apiResponse
    fun setApiResponse(apiResponse: Response<ApiResponseType>?) {
        this.apiResponse.value = apiResponse
    }
}