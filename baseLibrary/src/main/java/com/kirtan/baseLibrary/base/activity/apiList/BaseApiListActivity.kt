package com.kirtan.baseLibrary.base.activity.apiList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.base.dataHolder.BaseObject
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel.ApiStatus
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel.ApiStatus.*
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.baseLibrary.utils.parsedResponseForList.ListParsedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseApiListActivity<Screen : ViewDataBinding, ModelType : BaseObject, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ApiListCallingScreen<ApiRequestType, ApiResponseType, ListParsedResponse<ModelType>> {
    override val apiCallingViewModel: ApiListViewModel<ApiResponseType> by viewModels()
    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            INIT -> {
                apiCallingViewModel.dataStatus = DataNotParsed
                loadPage()
            }
            LOADING -> showPageProgress()
            COMPLETE -> gonePageProgress()
            else -> apiCallingStatus = INIT
        }
    }
    private val observeResponseStoredIntoLocal: Observer<Response<ApiResponseType>?> =
        Observer { response ->
            if (response == null) {
                showErrorOnDisplay(getString(R.string.server_unreachable))
                return@Observer
            }
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    showErrorOnDisplay(response.message())
                    return@Observer
                }
                apiCallingViewModel.setResponseData(body)
            } else {
                showErrorOnDisplay(response.message())
            }
        }
    private var apiCallingStatus: ApiStatus?
        get() = apiCallingViewModel.apiStatus.value
        set(value) {
            apiCallingViewModel.apiStatus.value = value
        }
    private var dataStatus: ApiListViewModel.Status
        get() = apiCallingViewModel.dataStatus
        set(value) {
            apiCallingViewModel.dataStatus = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiCallingViewModel.apiStatus.observe(this, apiStatusObserver)
        apiCallingViewModel.getApiResponse().observe(this, observeResponseStoredIntoLocal)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    private fun loadPage() {
        if (apiCallingStatus != INIT) return
        apiCallingStatus = LOADING
        apiCallingViewModel.loadApi { getApiCallingFunction(getApiRequest()) }
    }

    final override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this@BaseApiListActivity) { responseBody ->
            CoroutineScope(Dispatchers.Default).launch {
                if (dataStatus == DataNotParsed) {
                    dataStatus = DataParsing
                    val parsedResponse = parseListFromResponse(responseBody)
                    dataStatus = DataParsed
                    if (parsedResponse.isSuccess) {
                        withContext(Dispatchers.Main) {
                            getRecyclerView().post { models.addAll(parsedResponse.apiListData) }
                        }
                    } else {
                        withContext(Dispatchers.Main) { showErrorOnDisplay(parsedResponse.errorMessage) }
                    }
                }
            }
        }
    }

    override fun showPageProgress() {
        if (getSwipeRefreshLayout()?.isRefreshing == true) return
        super.showPageProgress()
    }

    override fun gonePageProgress() {
        getSwipeRefreshLayout()?.post { getSwipeRefreshLayout()?.isRefreshing = false }
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