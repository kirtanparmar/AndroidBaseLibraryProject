package com.kirtan.mylibrary.base.activity.apiPagingList

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.ListPagingScreen
import com.kirtan.mylibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.mylibrary.utils.PagingListModel
import com.kirtan.mylibrary.utils.gone
import com.kirtan.mylibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.mylibrary.utils.toast
import timber.log.Timber

abstract class BaseApiListPagingListActivity<Screen : ViewDataBinding, ModelType : PagingListModel, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ListPagingScreen<ModelType>,
    ApiListCallingScreen<ApiRequestType, ApiResponseType, PageListParsedResponse<ModelType>> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()

    private val firstPage: Int get() = getFirstPagePosition()
    private val dataLoaderModel by lazy { getLoaderDataModel() }

    private val pagingViewModel: PagingViewModel by viewModels()
    private var totalItems: Int
        get() = pagingViewModel.totalItems
        set(value) {
            pagingViewModel.totalItems = value
        }
    private var dataLoading: Boolean
        get() = pagingViewModel.dataLoading
        set(value) {
            pagingViewModel.dataLoading = value
        }
    private var page: Int
        get() = pagingViewModel.page
        set(value) {
            pagingViewModel.page = value
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
        observeApiResponse(apiCallingViewModel.getResponseData())
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
                if (adapter.itemCount < totalItems && !dataLoading) {
                    dataLoading = true
                    loadPage()
                }
            }
        }
    }

    private fun loadPage() {
        if (models.size > 0) getRecyclerView().post { models.add(getLoaderDataModel()) }
        showPageProgress()
        getApiCallingFunction(getApiRequest()).observe(this) { response ->
            Timber.d("$response")
            dataLoading = false
            if (models.size > 0) getRecyclerView().post { models.removeAt(models.size - 1) }
            gonePageProgress()
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    toast(response.message())
                    return@observe
                }
                apiCallingViewModel.setResponseData(body)
            } else {
                Timber.d("${response.code()} ${response.message()}")
                toast(response.message())
            }
        }
    }

    /**
     * Function should not be overridden.
     */
    override fun observeApiResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(this) { responseBody ->
            if (!apiCallingViewModel.dataFed) {
                apiCallingViewModel.dataFed = true
                parseListFromResponse(responseBody).observe(this) { parsedResponse ->
                    if (parsedResponse.isSuccess) {
                        totalItems = parsedResponse.newTotalItemCount
                        page++
                        getRecyclerView().post {
                            models.addAll(parsedResponse.newPageData) {
                                getRecyclerView().post { calculateForListingNewItems() }
                            }
                        }
                    } else showErrorOnDisplay(parsedResponse.errorMessage)
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
        if (getSwipeRefreshLayout()?.isRefreshing == true) {
            getSwipeRefreshLayout()?.isRefreshing = false
            return
        }
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

    /**
     * Function set the swipeRefreshLayout.
     */
    override fun setSwipeRefresh() {
        getSwipeRefreshLayout()?.setOnRefreshListener {
            getErrorTextView()?.gone()
            page = firstPage
            models.clear {
                dataLoading = true
                loadPage()
            }
        }
    }
}