package com.kirtan.mylibrary.base.fragment.apiPagingList

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.ListPagingScreen
import com.kirtan.mylibrary.base.fragment.BaseListFragment
import com.kirtan.mylibrary.base.viewModels.ApiViewModel
import com.kirtan.mylibrary.utils.PagingListModel
import com.kirtan.mylibrary.utils.gone
import com.kirtan.mylibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.mylibrary.utils.toast
import timber.log.Timber

abstract class BaseApiListPagingFragment<Screen : ViewDataBinding, ModelType : PagingListModel, ApiRequestType : Any?, ApiResponseType> :
    BaseListFragment<Screen, ModelType>(),
    ListPagingScreen<ModelType>,
    ApiListCallingScreen<ApiRequestType, ApiResponseType, PageListParsedResponse<ModelType>> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiViewModel: ApiViewModel<ApiResponseType> by viewModels()
    private val firstPage: Int get() = getFirstPagePosition()
    private var totalItems: Int = 0
    private var dataLoading: Boolean = false
    private var page: Int = firstPage
    private val dataLoaderModel by lazy { getLoaderDataModel() }

    /**
     * @return next page to be fetched from API
     */
    fun getPage() = page

    /**
     * Function will auto load api data, you only have to parse the response.
     * Function also set the paging on scroll of the recyclerView.
     * Function can be overridden as your need. In such case please call include super callback for the same function.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeApiResponse(apiViewModel.getResponseData())
        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Timber.v("onScrolled: dx: $dx, dy: $dy")
                if (!dataLoading && dy > 0) calculateForListingNewItems()
            }
        })
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
        getRecyclerView().post { if (models.size > 0) models.add(dataLoaderModel) }
        showPageProgress()
        getApiCallingFunction(getApiRequest()).observe(viewLifecycleOwner) { response ->
            Timber.d("$response")
            getRecyclerView().post { if (models.size > 0) models.removeAt(models.size - 1) }
            dataLoading = false
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
                apiViewModel.setResponseData(body)
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
        apiResponse.observe(viewLifecycleOwner) { responseBody ->
            parseListFromResponse(responseBody).observe(viewLifecycleOwner) { parsedResponse ->
                if (parsedResponse.isSuccess) {
                    totalItems = parsedResponse.newTotalItemCount
                    page++
                    getRecyclerView().post { models.addAll(parsedResponse.newPageData) }
                } else showErrorOnDisplay(parsedResponse.errorMessage)
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
            models.clear()
            dataLoading = true
            loadPage()
        }
    }
}