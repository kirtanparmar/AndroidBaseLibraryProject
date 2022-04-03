package com.kirtan.baseLibrary.base.activity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.base.ApiCallingScreen
import com.kirtan.baseLibrary.base.NetworkStatus
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiStatus
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.show
import com.kirtan.baseLibrary.utils.toast
import timber.log.Timber

abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    override val screenTag: String get() = "BaseAPIActivity"
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()

    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            is ApiStatus.Init -> loadAPIData()
            is ApiStatus.Loading -> {
                goneErrorOnDisplay()
                showPageProgress()
            }
            is ApiStatus.Success -> gonePageProgress()
            is ApiStatus.Error -> {
                Timber.d("$status")
                showErrorOnDisplay(status.error)
            }
        }
    }
    private var apiCallingStatus: ApiStatus
        get() = apiCallingViewModel.apiStatus.value ?: ApiStatus.Init
        set(value) {
            apiCallingViewModel.apiStatus.value = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSwipeRefreshLayout()?.setOnRefreshListener { apiCallingStatus = ApiStatus.Init }
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

    private fun loadAPIData() {
        if (apiCallingStatus !is ApiStatus.Init) return
        apiCallingStatus = ApiStatus.Loading
        apiCallingViewModel.loadApi { getApiCallingFunction(getApiRequest()) }
    }

    private fun showPageProgress() {
        goneErrorOnDisplay()
        getBody()?.gone()
        if (getSwipeRefreshLayout()?.isRefreshing == true) {
            return
        }
        getCenterProgressBar()?.show()
    }

    private fun gonePageProgress() {
        getSwipeRefreshLayout()?.post { getSwipeRefreshLayout()?.isRefreshing = false }
        getCenterProgressBar()?.gone()
        getBody()?.show()
    }

    private fun showErrorOnDisplay(error: String) {
        if (error.isBlank()) {
            return
        }
        getErrorTextView()?.show()
        getErrorTextView()?.text = error
        getErrorView()?.show()
        if (getErrorTextView() == null) {
            toast(error)
        }
    }

    private fun goneErrorOnDisplay() {
        getErrorTextView()?.gone()
        getErrorTextView()?.text = ""
        getErrorView()?.gone()
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