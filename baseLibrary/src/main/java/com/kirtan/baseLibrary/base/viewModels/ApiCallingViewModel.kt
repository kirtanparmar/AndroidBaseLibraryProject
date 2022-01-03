package com.kirtan.baseLibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber

open class ApiCallingViewModel<ApiResponseType> : ViewModel() {
    var apiStatus: MutableLiveData<ApiStatus> = MutableLiveData(ApiStatus.INIT)

    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()
    fun getResponseData(): LiveData<ApiResponseType> = responseData
    fun setResponseData(responseData: ApiResponseType) {
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
    private fun setApiResponse(apiResponse: Response<ApiResponseType>?) {
        this.apiResponse.value = apiResponse
    }

    fun loadApi(api: suspend () -> Response<ApiResponseType>?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val apiCall = api.invoke()
                withContext(Dispatchers.Main) { setApiResponse(apiCall) }
            }
        }
    }
}