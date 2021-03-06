package com.kirtan.baseLibrary.apiHelpers

import android.net.ConnectivityManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kirtan.baseLibrary.base.NetworkStatus
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel
import retrofit2.Response

interface ApiCallingScreen<ApiRequestType : Any?, ApiResponse> {
    val apiCallingViewModel: ApiCallingViewModel<ApiResponse>
    val networkCallback: ConnectivityManager.NetworkCallback

    suspend fun getApiCallingFunction(apiRequest: ApiRequestType): Response<ApiResponse>?

    fun getApiRequest(): ApiRequestType

    fun observeApiDataResponse(apiResponse: LiveData<ApiResponse>)

    fun getCenterProgressBar(): ProgressBar?

    fun getSwipeRefreshLayout(): SwipeRefreshLayout?

    fun getBody(): View?

    fun getErrorTextView(): TextView?

    fun getErrorView(): View?

    fun getNoNetworkView(): View?

    fun onNetworkStateChange(networkStatus: NetworkStatus) {}
}