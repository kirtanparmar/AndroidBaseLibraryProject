package com.kirtan.baseLibrary.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.kirtan.baseLibrary.R
import com.kirtan.baseLibrary.base.ApiCallingScreen
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.baseLibrary.base.viewModels.ApiCallingViewModel.ApiStatus.*
import com.kirtan.baseLibrary.utils.gone
import com.kirtan.baseLibrary.utils.show
import com.kirtan.baseLibrary.utils.toast
import retrofit2.Response

abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    override val tag: String get() = "BaseAPIActivity"

    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()
    private val apiStatusObserver: Observer<ApiCallingViewModel.ApiStatus> = Observer { status ->
        when (status) {
            INIT -> loadAPIData()
            COMPLETE -> gonePageProgress()
            LOADING -> showPageProgress()
            null -> {
                apiCallingStatus = INIT
            }
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
            } else showErrorOnDisplay(response.message())
        }

    private var apiCallingStatus: ApiCallingViewModel.ApiStatus?
        get() = apiCallingViewModel.apiStatus.value
        set(value) {
            apiCallingViewModel.apiStatus.value = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSwipeRefreshLayout()?.setOnRefreshListener {
            apiCallingStatus = INIT
        }
        apiCallingViewModel.apiStatus.observe(this, apiStatusObserver)
        apiCallingViewModel.getApiResponse().observe(this, observeResponseStoredIntoLocal)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    private fun loadAPIData() {
        if (apiCallingStatus != INIT) return
        apiCallingStatus = LOADING
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