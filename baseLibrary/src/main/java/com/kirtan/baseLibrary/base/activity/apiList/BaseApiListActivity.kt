package com.kirtan.baseLibrary.base.activity.apiList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.base.dataHolder.BaseObject
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.baseLibrary.base.viewModels.ApiStatus
import com.kirtan.baseLibrary.utils.parsedResponseForList.ListParsedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseApiListActivity<Screen : ViewDataBinding, ModelType : BaseObject, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ApiListCallingScreen<ApiRequestType, ApiResponseType, ListParsedResponse<ModelType>> {
    override val apiCallingViewModel: ApiListViewModel<ApiResponseType> by viewModels()
    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            is ApiStatus.Error -> {
                Timber.d("$status")
                gonePageProgress()
                showErrorOnDisplay(status.error)
            }
            ApiStatus.Init -> {
                apiCallingViewModel.dataStatus = DataNotParsed
                loadPage()
            }
            ApiStatus.Loading -> showPageProgress()
            ApiStatus.Success -> gonePageProgress()
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
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    private fun loadPage() {
        if (apiCallingStatus != ApiStatus.Init) return
        apiCallingStatus = ApiStatus.Loading
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
        apiCallingStatus = ApiStatus.Init
    }
}