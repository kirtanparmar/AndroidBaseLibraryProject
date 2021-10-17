package com.kirtan.mylibrary.base.activity.apiList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.mylibrary.base.dataHolder.BaseObject
import com.kirtan.mylibrary.base.viewModels.ApiViewModel
import com.kirtan.mylibrary.utils.parsedResponseForList.ListParsedResponse
import timber.log.Timber

/**
 * Extend this class when you want to use the listing with api in your activity.
 */
abstract class BaseApiListActivity<Screen : ViewDataBinding, ModelType : BaseObject, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ApiListCallingScreen<ApiRequestType, ApiResponseType, ListParsedResponse<ModelType>> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiViewModel: ApiViewModel<ApiResponseType> by viewModels()
    private var apiStatus: ApiViewModel.ApiStatus?
        get() = apiViewModel.status.value
        set(value) {
            apiViewModel.status.value = value
        }

    /**
     * This function will auto load api data, you only have to parse the response.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (apiStatus == ApiViewModel.ApiStatus.INITIATE) loadPage()
        observeApiResponse(apiViewModel.getResponseData())
        apiViewModel.status.observe(this) { status ->
            when (status) {
                ApiViewModel.ApiStatus.INITIATE, ApiViewModel.ApiStatus.LOADING -> showPageProgress()
                ApiViewModel.ApiStatus.LOADED, null -> gonePageProgress()
            }
        }
    }

    /**
     * Function loads the data from api and calls the #observeApiResponse function on success.
     */
    private fun loadPage() {
        Timber.d("Loading API Data")
        if (apiStatus != ApiViewModel.ApiStatus.INITIATE) return
        apiStatus = ApiViewModel.ApiStatus.LOADING
        getApiCallingFunction(getApiRequest()).observe(this) { response ->
            Timber.d("$response")
            if (response == null) {
                showErrorOnDisplay(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    showErrorOnDisplay(response.message())
                    return@observe
                }
                apiViewModel.setResponseData(body)
            } else {
                Timber.d("${response.code()} ${response.message()}")
                showErrorOnDisplay(response.message())
            }
        }
    }

    /**
     * Function will observe the api response and will request for parsing the response into the list of models to be listed in the list screen.
     */
    override fun observeApiResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this@BaseApiListActivity) { responseBody ->
            parseListFromResponse(responseBody).observe(this@BaseApiListActivity) { parsedResponse ->
                if (parsedResponse.isSuccess) {
                    if (apiStatus == ApiViewModel.ApiStatus.LOADING) {
                        apiStatus = ApiViewModel.ApiStatus.LOADED
                        Timber.d("API Data Loaded : Success")
                        getRecyclerView().post {
                            models.addAll(parsedResponse.apiListData)
                        }
                    }
                } else {
                    Timber.d("API Data Loaded : Failed")
                    showErrorOnDisplay(parsedResponse.errorMessage)
                }
            }
        }
    }

    /**
     * Function will display the loader according the need.
     */
    override fun showPageProgress() {
        if (getSwipeRefreshLayout()?.isRefreshing == true) return
        super.showPageProgress()
    }

    /**
     * Function will hide the loader according the need.
     */
    override fun gonePageProgress() {
        if (getSwipeRefreshLayout()?.isRefreshing == true) {
            getSwipeRefreshLayout()?.isRefreshing = false
            return
        }
        super.gonePageProgress()
    }

    /**
     * Function will set SwipeToRefresh layout.
     */
    override fun setSwipeRefresh() {
        getSwipeRefreshLayout()?.setOnRefreshListener {
            apiStatus = ApiViewModel.ApiStatus.INITIATE
            hideErrorOnDisplay()
            models.clear()
            loadPage()
        }
    }
}