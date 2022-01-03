package com.kirtan.baseLibrary.base.activity.apiPagingList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.ListPagingScreen
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel.ApiStatus.*
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.baseLibrary.utils.PagingListModel
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.baseLibrary.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber

abstract class BaseApiListPagingListActivity<Screen : ViewDataBinding, ModelType : PagingListModel, ApiRequestType : Any?, ApiResponseType : PagingApiResponse> :
    BaseListActivity<Screen, ModelType>(),
    ListPagingScreen<ModelType>,
    ApiListCallingScreen<ApiRequestType, ApiResponseType, PageListParsedResponse<ModelType>> {

    private val firstPage: Int get() = getFirstPagePosition()
    private val dataLoaderModel by lazy { getLoaderDataModel() }

    override val apiCallingViewModel: ApiPagingViewModel<ApiResponseType> by viewModels()
    private var apiCallingStatus: ApiCallingViewModel.ApiStatus?
        get() = apiCallingViewModel.apiStatus.value
        set(value) {
            apiCallingViewModel.apiStatus.value = value
        }
    private var totalPagination: Int
        get() = apiCallingViewModel.paginationInfo.total
        set(value) {
            apiCallingViewModel.paginationInfo.total = value
        }
    private var page: Int
        get() = apiCallingViewModel.page
        set(value) {
            apiCallingViewModel.page = value
        }
    private var dataStatus: ApiListViewModel.Status
        get() = apiCallingViewModel.dataStatus
        set(value) {
            apiCallingViewModel.dataStatus = value
        }
    private val apiStatusObserver: Observer<ApiCallingViewModel.ApiStatus> = Observer { status ->
        when (status) {
            INIT -> {
                if (page == -1) page = firstPage
                dataStatus = DataNotParsed
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
                toast(getString(R.string.server_unreachable))
                return@Observer
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    toast(response.message())
                    return@Observer
                }
                if (!body.dataParsed) {
                    apiCallingViewModel.setResponseData(body)
                }
            } else {
                Timber.d("${response.code()} ${response.message()}")
                toast(response.message())
            }
        }

    /**
     * @return next page to be fetched from API
     */
    fun getCurrentPage() = page

    /**
     * Function will auto load api data, you only have to parse the response.
     * Function also set the paging on scroll of the recyclerView.
     * Function can be overridden as your need. In such case please call include super callback for the same function.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiCallingViewModel.getApiResponse().observe(this, observeResponseStoredIntoLocal)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
        apiCallingViewModel.apiStatus.observe(this, apiStatusObserver)
        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Timber.v("onScrolled: dx: $dx, dy: $dy")
                if (apiCallingStatus == COMPLETE && dy > 0) calculateForListingNewItems()
            }
        })
    }

    private fun calculateForListingNewItems() {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        if (layoutManager is LinearLayoutManager) {
            val firstVisibleItemIndex =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if ((visibleItemCount + firstVisibleItemIndex) >= totalItemCount) {
                if (apiCallingViewModel.paginationInfo.paginationOn == PaginationOn.ITEM_COUNT) {
                    if (adapter.itemCount < totalPagination && apiCallingStatus == COMPLETE) {
                        apiCallingStatus = INIT
                    }
                } else if (apiCallingViewModel.paginationInfo.paginationOn == PaginationOn.PAGE) {
                    if (page <= totalPagination && apiCallingStatus == COMPLETE) {
                        apiCallingStatus = INIT
                    }
                }
            }
        }
    }

    private fun loadPage() {
        if (apiCallingStatus != INIT) return
        apiCallingStatus = LOADING
        apiCallingViewModel.loadApi { getApiCallingFunction(getApiRequest()) }
    }

    /**
     * Function should not be overridden.
     */
    final override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this) { responseBody ->
            CoroutineScope(Dispatchers.Default).launch {
                if (!responseBody.dataParsed && dataStatus == DataNotParsed) {
                    responseBody.dataParsed = true
                    dataStatus = DataParsing
                    val parsedResponse = parseListFromResponse(responseBody)
                    dataStatus = DataParsed
                    if (parsedResponse.isSuccess) {
                        totalPagination = parsedResponse.newTotalPagination
                        page++
                        withContext(Dispatchers.Main) {
                            getRecyclerView().post {
                                models.addAll(parsedResponse.newPageData) {
                                    getRecyclerView().post { calculateForListingNewItems() }
                                }
                            }
                        }
                    } else withContext(Dispatchers.Main) { showErrorOnDisplay(parsedResponse.errorMessage) }
                }
            }
        }
    }

    /**
     * Function should not be overridden.
     */
    override fun showPageProgress() {
        if (models.size > 0) {
            if (!models.last().isLoaderModel) getRecyclerView().post { models.add(getLoaderDataModel()) }
        } else {
            if (getSwipeRefreshLayout()?.isRefreshing == true) return
            if (page == firstPage) super.showPageProgress()
        }
    }

    /**
     * Function should not be overridden.
     */
    override fun gonePageProgress() {
        if (models.size > 0) {
            if (models.last().isLoaderModel) getRecyclerView().post { models.removeAt(models.size - 1) }
        }
        getSwipeRefreshLayout()?.isRefreshing = false
        super.gonePageProgress()
    }

    /**
     * Function should not be overridden.
     */
    override fun showErrorOnDisplay(text: String) {
        if (page - 1 == firstPage) super.showErrorOnDisplay(text)
        else {
            toast(text)
            getErrorTextView()?.gone()
        }
    }

    override fun onSwipeRefreshDoExtra() {
        page = firstPage
        models.clear { apiCallingStatus = INIT }
    }
}