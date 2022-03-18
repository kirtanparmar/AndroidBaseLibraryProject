package com.kirtan.baseLibrary.base.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

open class ApiCallingViewModel<ApiResponseType> : ViewModel() {
    var apiStatus: MutableLiveData<ApiStatus> = MutableLiveData(ApiStatus.Init)

    private val responseData: MutableLiveData<ApiResponseType> = MutableLiveData()
    fun getResponseData(): LiveData<ApiResponseType> = responseData

    fun loadApi(apiFunction: suspend () -> Response<ApiResponseType>?) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiFunction.invoke()
            if (response == null) {
                apiStatus.value = ApiStatus.Error("Server Unreachable", -1)
                return@launch
            }
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    apiStatus.value = ApiStatus.Error(response.message(), response.code())
                    return@launch
                }
                withContext(Dispatchers.Main) { apiStatus.value = ApiStatus.Success }
                withContext(Dispatchers.Main) { responseData.postValue(body) }
            } else withContext(Dispatchers.Main) {
                apiStatus.value = ApiStatus.Error(response.message(), response.code())
            }
        }
    }
}