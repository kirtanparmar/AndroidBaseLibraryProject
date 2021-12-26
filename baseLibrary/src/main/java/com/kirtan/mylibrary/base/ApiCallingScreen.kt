package com.kirtan.mylibrary.base

import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import retrofit2.Response

interface ApiCallingScreen<ApiRequestType : Any?, ApiResponse> {
    /**
     * this viewModel object must be created.
     */
    val apiCallingViewModel: ApiCallingViewModel<ApiResponse>

    /**
     * @param apiRequest created apiRequest is automatically passed which you have created.
     * @return Provide viewModel Api call function, which will bw used for auto calling api by this class @function @loadAPIData.
     * */
    suspend fun getApiCallingFunction(apiRequest: ApiRequestType): Response<ApiResponse>?

    /**
     * @return Type of the request which you are going to pass in the request body.
     */
    fun getApiRequest(): ApiRequestType

    /**
     * Observe the response which will be the type of #ApiResponse
     */
    fun observeApiDataResponse(apiResponse: LiveData<ApiResponse>)

    /**
     * @return should be the center aligned progressBar if you are using else can be null.
     */
    fun getCenterProgressBar(): ProgressBar?

    /**
     * @return should be the swipe refresh layout else can be null.
     */
    fun getSwipeRefreshLayout(): SwipeRefreshLayout?

    /**
     * @return this view should be containing all the views in it except the center aligned progressBar.
     */
    fun getBody(): View?

    /**
     * This view is for displaying error message.
     * @return @MaterialTextView or @null.
     */
    fun getErrorTextView(): MaterialTextView?

    /**
     * This view is for displaying error graphics.
     * @return @View or @null.
     */
    fun getErrorView(): View?
}