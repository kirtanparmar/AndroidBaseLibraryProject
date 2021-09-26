package com.kirtan.mylibrary.base

import androidx.lifecycle.LiveData
import com.kirtan.mylibrary.base.viewModels.ApiViewModel
import retrofit2.Response

interface ApiCallingScreen<ApiRequestType : Any?, ApiResponse> {
    /**
     * this viewModel object must be created.
     */
    val apiViewModel: ApiViewModel<ApiResponse>

    /**
     * @param apiRequest created apiRequest is automatically passed which you have created.
     * @return Provide viewModel Api call function, which will bw used for auto calling api by this class @function @loadAPIData.
     * */
    fun getApiCallingFunction(apiRequest: ApiRequestType): LiveData<Response<ApiResponse>?>

    /**
     * @return Type of the request which you are going to pass in the request body.
     */
    fun getApiRequest(): ApiRequestType

    /**
     * Observe the response which will be the type of #ApiResponse
     */
    fun observeApiResponse(apiResponse: LiveData<ApiResponse>)
}