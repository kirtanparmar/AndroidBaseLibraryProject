package com.kirtan.mylibrary.base.activity.apiList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.mylibrary.base.dataHolder.BaseObject
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel.ApiStatus
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel.ApiStatus.*
import com.kirtan.mylibrary.base.viewModels.ApiListViewModel
import com.kirtan.mylibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.mylibrary.utils.parsedResponseForList.ListParsedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
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
    private val apiListViewModel: ApiListViewModel by viewModels()
    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            INIT -> {
                apiListViewModel.status = DataNotParsed
                loadPage()
            }
            LOADING -> showPageProgress()
            COMPLETE -> gonePageProgress()
            else -> apiCallingStatus = INIT
        }
    }
    private val observeResponseFromApi: Observer<Response<ApiResponseType>?> =
        Observer { apiResponse -> apiCallingViewModel.setApiResponse(apiResponse) }
    private val observeResponseStoredIntoLocal: Observer<Response<ApiResponseType>?> =
        Observer { response ->
            Timber.d("$response")
            if (response == null) {
                showErrorOnDisplay(getString(R.string.server_unreachable))
                return@Observer
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    showErrorOnDisplay(response.message())
                    return@Observer
                }
                apiCallingViewModel.setResponseData(body)
            } else {
                Timber.d("${response.code()} ${response.message()}")
                showErrorOnDisplay(response.message())
            }
        }
    private var apiCallingStatus: ApiStatus?
        get() = apiCallingViewModel.status.value
        set(value) {
            apiCallingViewModel.status.value = value
        }

    /**
     * This function will auto load api data, you only have to parse the response.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiCallingViewModel.status.observe(this, apiStatusObserver)
        apiCallingViewModel.getApiResponse().observe(this, observeResponseStoredIntoLocal)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    /**
     * Function loads the data from api and calls the #observeApiResponse function on success.
     */
    private fun loadPage() {
        if (apiCallingStatus == LOADING) return
        apiCallingStatus = LOADING
        getApiCallingFunction(getApiRequest()).observe(this, observeResponseFromApi)
    }

    /**
     * Function will observe the api response and will request for parsing the response into the list of models to be listed in the list screen.
     */
    override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this@BaseApiListActivity) { responseBody ->
            CoroutineScope(Dispatchers.Default).launch {
                if (apiListViewModel.status == DataNotParsed) {
                    apiListViewModel.status = DataParsing
                    val parsedResponse = parseListFromResponse(responseBody)
                    apiListViewModel.status = DataParsed
                    if (parsedResponse.isSuccess) {
                        Timber.d("API Data Loaded : Success")
                        withContext(Dispatchers.Main) {
                            getRecyclerView().post { models.addAll(parsedResponse.apiListData) }
                        }
                    } else {
                        Timber.d("API Data Loaded : Failed")
                        withContext(Dispatchers.Main) { showErrorOnDisplay(parsedResponse.errorMessage) }
                    }
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

    override fun onSwipeRefreshDoExtra() {
        apiCallingStatus = INIT
    }

    override fun onDestroy() {
        if (apiCallingStatus == LOADING) apiCallingStatus = INIT
        super.onDestroy()
    }
}