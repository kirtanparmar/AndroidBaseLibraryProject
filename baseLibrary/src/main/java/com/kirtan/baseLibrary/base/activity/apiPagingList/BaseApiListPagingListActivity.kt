package com.kirtan.baseLibrary.base.activity.apiPagingList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.ListPagingScreen
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.utils.PagingListModel
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.baseLibrary.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseApiListPagingListActivity<Screen : ViewDataBinding, ModelType : PagingListModel, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ListPagingScreen<ModelType>,
    ApiListCallingScreen<ApiRequestType, ApiResponseType, PageListParsedResponse<ModelType>> {

    private val firstPage: Int get() = getFirstPagePosition()
    private val dataLoaderModel by lazy { getLoaderDataModel() }

    override val apiCallingViewModel: ApiPagingViewModel<ApiResponseType> by viewModels()
    private var totalPagination: Int
        get() = apiCallingViewModel.paginationInfo.total
        set(value) {
            apiCallingViewModel.paginationInfo.total = value
        }
    private var dataLoading: Boolean
        get() = apiCallingViewModel.dataLoading
        set(value) {
            apiCallingViewModel.dataLoading = value
        }
    private var page: Int
        get() = apiCallingViewModel.page
        set(value) {
            apiCallingViewModel.page = value
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
        observeApiDataResponse(apiCallingViewModel.getResponseData())
        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Timber.v("onScrolled: dx: $dx, dy: $dy")
                if (!dataLoading && dy > 0) calculateForListingNewItems()
            }
        })
        if (page == -1) {
            page = firstPage
        }
        if (page == firstPage) {
            dataLoading = true
            loadPage()
        }
    }

    private fun calculateForListingNewItems() {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        if (layoutManager is LinearLayoutManager) {
            val firstVisibleItemIndex =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if ((visibleItemCount + firstVisibleItemIndex) >= totalItemCount) {
                if (apiCallingViewModel.paginationInfo.paginationOn == PaginationOn.ITEM_COUNT) {
                    if (adapter.itemCount < totalPagination && !dataLoading) {
                        dataLoading = true
                        loadPage()
                    }
                } else if (apiCallingViewModel.paginationInfo.paginationOn == PaginationOn.PAGE) {
                    if (page <= totalPagination && !dataLoading) {
                        dataLoading = true
                        loadPage()
                    }
                }
            }
        }
    }

    private fun loadPage() {
        if (models.size > 0) getRecyclerView().post { models.add(getLoaderDataModel()) }
        showPageProgress()
        CoroutineScope(Dispatchers.IO).launch {
            val response = getApiCallingFunction(getApiRequest())
            Timber.d("$response")
            dataLoading = false
            if (models.size > 0) getRecyclerView().post { models.removeAt(models.size - 1) }
            gonePageProgress()
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@launch
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    toast(response.message())
                    return@launch
                }
                withContext(Dispatchers.Main) { apiCallingViewModel.setResponseData(body) }
            } else {
                Timber.d("${response.code()} ${response.message()}")
                toast(response.message())
            }
        }
    }

    /**
     * Function should not be overridden.
     */
    override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this) { responseBody ->
            if (!apiCallingViewModel.dataFed) {
                apiCallingViewModel.dataFed = true
                CoroutineScope(Dispatchers.Default).launch {
                    val parsedResponse = parseListFromResponse(responseBody)
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
        if (getSwipeRefreshLayout()?.isRefreshing == true) return
        if (page == firstPage) super.showPageProgress()
    }

    /**
     * Function should not be overridden.
     */
    override fun gonePageProgress() {
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
        models.clear {
            dataLoading = true
            loadPage()
        }
    }
}