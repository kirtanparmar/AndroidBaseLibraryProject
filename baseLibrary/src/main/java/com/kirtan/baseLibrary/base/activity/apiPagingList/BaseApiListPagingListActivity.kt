package com.kirtan.baseLibrary.base.activity.apiPagingList

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.ListPagingScreen
import com.kirtan.baseLibrary.base.NetworkStatus
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.base.dataHolder.Operation
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.baseLibrary.base.viewModels.ApiStatus
import com.kirtan.baseLibrary.utils.PagingListModel
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.baseLibrary.utils.show
import com.kirtan.baseLibrary.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseApiListPagingListActivity<Screen : ViewDataBinding, ModelType : PagingListModel, ApiRequestType : Any?, ApiResponseType : PagingApiResponse> :
    BaseListActivity<Screen, ModelType>(),
    ListPagingScreen<ModelType>,
    ApiListCallingScreen<ApiRequestType, ApiResponseType, PageListParsedResponse<ModelType>> {

    override val apiCallingViewModel: ApiPagingViewModel<ApiResponseType> by viewModels()
    override val screenTag: String get() = "BaseApiListPagingListActivity"

    private val firstPage: Int get() = getFirstPagePosition()
    private val dataLoaderModel by lazy { getLoaderDataModel() }
    private var apiCallingStatus: ApiStatus
        get() = apiCallingViewModel.apiStatus.value ?: ApiStatus.Init
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
    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            is ApiStatus.Error -> {
                Timber.d("$status")
                gonePageProgress()
                showErrorOnDisplay(status.error)
            }
            ApiStatus.Init -> {
                if (page == -1) page = firstPage
                dataStatus = DataNotParsed
                loadPage()
            }
            ApiStatus.Loading -> {
                goneErrorOnDisplay()
                showPageProgress()
            }
            ApiStatus.Success -> gonePageProgress()
        }
    }

    fun getCurrentPage() = page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
        apiCallingViewModel.apiStatus.observe(this, apiStatusObserver)
        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (apiCallingStatus == ApiStatus.Success && dy > 0) calculateForListingNewItems()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (getSystemService(ConnectivityManager::class.java)
                    ).registerDefaultNetworkCallback(networkCallback)
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (getSystemService(ConnectivityManager::class.java)
                    ).unregisterNetworkCallback(networkCallback)
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
                    if (adapter.itemCount < totalPagination && apiCallingStatus == ApiStatus.Success) {
                        apiCallingStatus = ApiStatus.Init
                    }
                } else if (apiCallingViewModel.paginationInfo.paginationOn == PaginationOn.PAGE) {
                    if (page <= totalPagination && apiCallingStatus == ApiStatus.Success) {
                        apiCallingStatus = ApiStatus.Init
                    }
                }
            }
        }
    }

    private fun loadPage() {
        if (apiCallingStatus != ApiStatus.Init) return
        apiCallingStatus = ApiStatus.Loading
        apiCallingViewModel.loadApi { getApiCallingFunction(getApiRequest()) }
    }

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
                                models.addAll(parsedResponse.newPageData) { operation ->
                                    when (operation) {
                                        is Operation.Error -> showErrorOnDisplay(operation.message)
                                        is Operation.Fail -> showErrorOnDisplay(operation.message)
                                        Operation.Success -> getRecyclerView().post { calculateForListingNewItems() }
                                    }
                                }
                            }
                        }
                    } else withContext(Dispatchers.Main) { showErrorOnDisplay(parsedResponse.errorMessage) }
                }
            }
        }
    }

    override fun showPageProgress() {
        if (models.size > 0) {
            if (!models.last().isLoaderModel) getRecyclerView().post { models.add(getLoaderDataModel()) }
        } else {
            if (getSwipeRefreshLayout()?.isRefreshing == true) return
            if (page == firstPage) super.showPageProgress()
        }
    }

    override fun gonePageProgress() {
        if (models.size > 0) {
            if (models.last().isLoaderModel) getRecyclerView().post { models.removeAt(models.size - 1) }
        }
        getSwipeRefreshLayout()?.isRefreshing = false
        super.gonePageProgress()
    }

    override fun showErrorOnDisplay(text: String) {
        if (page - 1 == firstPage) super.showErrorOnDisplay(text)
        else {
            toast(text)
            getErrorTextView()?.gone()
        }
    }

    override fun onSwipeRefreshDoExtra() {
        page = firstPage
        models.clear { operation ->
            when (operation) {
                is Operation.Error -> showErrorOnDisplay(operation.message)
                is Operation.Fail -> showErrorOnDisplay(operation.message)
                Operation.Success -> apiCallingStatus = ApiStatus.Init
            }
        }
    }

    final override val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Timber.d("networkChange internet connected")
            getNoNetworkView()?.gone()
            screen.root.post {
                if (apiCallingStatus is ApiStatus.Error) {
                    apiCallingStatus = ApiStatus.Init
                }
            }
            onNetworkStateChange(NetworkStatus.NetworkConnected)
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            Timber.d("networkChange Unmetered: $unmetered")
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            Timber.d("networkChange internet lost")
            getNoNetworkView()?.show()
            onNetworkStateChange(NetworkStatus.NetworkDisconnected)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Timber.d("networkChange internet unavailable")
        }
    }
}