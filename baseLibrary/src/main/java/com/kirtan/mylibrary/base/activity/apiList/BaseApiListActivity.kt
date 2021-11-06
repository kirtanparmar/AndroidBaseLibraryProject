package com.kirtan.mylibrary.base.activity.apiList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.mylibrary.base.dataHolder.BaseObject
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
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
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()
    private var apiCallingStatus: ApiCallingViewModel.ApiStatus?
        get() = apiCallingViewModel.status.value
        set(value) {
            apiCallingViewModel.status.value = value
        }

    /**
     * This function will auto load api data, you only have to parse the response.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (apiCallingStatus == ApiCallingViewModel.ApiStatus.INIT) loadPage()
        observeApiDataResponse(apiCallingViewModel.getResponseData())
        apiCallingViewModel.status.observe(this) { status ->
            when (status) {
                ApiCallingViewModel.ApiStatus.INIT, ApiCallingViewModel.ApiStatus.LOADING -> showPageProgress()
                ApiCallingViewModel.ApiStatus.COMPLETE, null -> gonePageProgress()
            }
        }
    }

    /**
     * Function loads the data from api and calls the #observeApiResponse function on success.
     */
    private fun loadPage() {
        Timber.d("Loading API Data")
        if (apiCallingStatus != ApiCallingViewModel.ApiStatus.INIT) return
        apiCallingStatus = ApiCallingViewModel.ApiStatus.LOADING
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
                apiCallingViewModel.setResponseData(body)
            } else {
                Timber.d("${response.code()} ${response.message()}")
                showErrorOnDisplay(response.message())
            }
        }
    }

    /**
     * Function will observe the api response and will request for parsing the response into the list of models to be listed in the list screen.
     */
    override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this@BaseApiListActivity) { responseBody ->
            parseListFromResponse(responseBody).observe(this@BaseApiListActivity) { parsedResponse ->
                if (parsedResponse.isSuccess) {
                    if (apiCallingStatus == ApiCallingViewModel.ApiStatus.LOADING) {
                        apiCallingStatus = ApiCallingViewModel.ApiStatus.COMPLETE
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
            apiCallingStatus = ApiCallingViewModel.ApiStatus.INIT
            hideErrorOnDisplay()
            models.clear()
            loadPage()
        }
    }
}