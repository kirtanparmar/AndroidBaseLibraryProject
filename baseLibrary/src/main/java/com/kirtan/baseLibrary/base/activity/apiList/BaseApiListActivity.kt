package com.kirtan.baseLibrary.base.activity.apiList

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.base.ApiListCallingScreen
import com.kirtan.baseLibrary.base.NetworkStatus
import com.kirtan.baseLibrary.base.activity.listActivity.BaseListActivity
import com.kirtan.baseLibrary.base.dataHolder.BaseObject
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiListViewModel.Status.*
import com.kirtan.baseLibrary.base.viewModels.ApiStatus
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.parsedResponseForList.ListParsedResponse
import com.kirtan.baseLibrary.utils.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseApiListActivity<Screen : ViewDataBinding, ModelType : BaseObject, ApiRequestType : Any?, ApiResponseType> :
    BaseListActivity<Screen, ModelType>(),
    ApiListCallingScreen<ApiRequestType, ApiResponseType, ListParsedResponse<ModelType>> {
    override val apiCallingViewModel: ApiListViewModel<ApiResponseType> by viewModels()
    override val screenTag: String get() = "BaseApiListActivity"

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
            ApiStatus.Loading -> {
                goneErrorOnDisplay()
                showPageProgress()
            }
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