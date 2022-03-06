package com.kirtan.baseLibrary.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.base.ApiCallingScreen
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiStatus
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.show
import com.kirtan.baseLibrary.utils.toast
import timber.log.Timber

abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    override val tag: String get() = "BaseAPIActivity"

    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()
    private val apiStatusObserver: Observer<ApiStatus> = Observer { status ->
        when (status) {
            is ApiStatus.Init -> loadAPIData()
            is ApiStatus.Loading -> showPageProgress()
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
}