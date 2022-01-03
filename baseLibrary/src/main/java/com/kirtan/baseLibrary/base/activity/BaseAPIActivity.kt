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
import timber.log.Timber

/**
 * This class should be used when you need to call the apis in your activity.
 */
abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    override val tag: String get() = "BaseAPIActivity"

    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()
    private val apiStatusObserver: Observer<ApiCallingViewModel.ApiStatus> = Observer { status ->
        Timber.d(tag, "apiStatusObserver: newStatus:- $status")
        when (status) {
            INIT -> loadAPIData()
            COMPLETE -> gonePageProgress()
            LOADING -> showPageProgress()
            null -> {
                Timber.d(
                    tag,
                    "apiStatusObserver: Found null type of api status, resetting to $INIT."
                )
                apiCallingStatus = INIT
            }
        }
    }
    private val observeResponseStoredIntoLocal: Observer<Response<ApiResponseType>?> =
        Observer { response ->
            Timber.d(tag, "observeResponseStoredIntoLocal: $response")
            if (response == null) {
                Timber.d(tag, "observeResponseStoredIntoLocal: response is null.")
                showErrorOnDisplay(getString(R.string.server_unreachable))
                return@Observer
            }
            if (response.isSuccessful) {
                Timber.d(tag, "observeResponseStoredIntoLocal: response is successful.")
                val body = response.body()
                if (body == null) {
                    Timber.d(tag, "observeResponseStoredIntoLocal: response body null.")
                    showErrorOnDisplay(response.message())
                    return@Observer
                }
                Timber.d(tag, "observeResponseStoredIntoLocal: response body: $body.")
                apiCallingViewModel.setResponseData(body)
            } else showErrorOnDisplay(response.message())
        }

    private var apiCallingStatus: ApiCallingViewModel.ApiStatus?
        get() = apiCallingViewModel.apiStatus.value
        set(value) {
            apiCallingViewModel.apiStatus.value = value
        }

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d(tag, "onCreate: ")
        getSwipeRefreshLayout()?.setOnRefreshListener {
            Timber.d(tag, "setOnRefreshListener: refreshing page.")
            apiCallingStatus = INIT
        }
        apiCallingViewModel.apiStatus.observe(this, apiStatusObserver)
        apiCallingViewModel.getApiResponse().observe(this, observeResponseStoredIntoLocal)
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    /**
     * This function loads the data through the api.
     */
    private fun loadAPIData() {
        Timber.d(tag, "loadAPIData: ")
        if (apiCallingStatus == LOADING) {
            Timber.d(tag, "loadAPIData: api already loading.")
            return
        }
        apiCallingStatus = LOADING
        Timber.d(tag, "loadAPIData: calling api.")
        apiCallingViewModel.loadApi { getApiCallingFunction(getApiRequest()) }
    }

    /**
     * Function will display the loader according the need.
     */
    private fun showPageProgress() {
        Timber.d(tag, "showPageProgress: ")
        goneErrorOnDisplay()
        getBody()?.gone()
        if (getSwipeRefreshLayout()?.isRefreshing == true) {
            Timber.d(tag, "showPageProgress: swipeRefreshLayout is refreshing")
            return
        }
        getCenterProgressBar()?.show()
    }

    /**
     * Function will hide the loader according the need.
     */
    private fun gonePageProgress() {
        Timber.d(tag, "gonePageProgress: ")
        getSwipeRefreshLayout()?.post { getSwipeRefreshLayout()?.isRefreshing = false }
        getCenterProgressBar()?.gone()
        getBody()?.show()
    }

    private fun showErrorOnDisplay(error: String) {
        Timber.d(tag, "showErrorOnDisplay: ")
        if (error.isBlank()) {
            Timber.d(tag, "showErrorOnDisplay: error is empty.")
            return
        }
        getErrorTextView()?.show()
        getErrorTextView()?.text = error
        getErrorView()?.show()
        if (getErrorTextView() == null) {
            Timber.d(tag, "showErrorOnDisplay: errorTextView is null, showing error toast.")
            toast(error)
        }
    }

    private fun goneErrorOnDisplay() {
        Timber.d(tag, "goneErrorOnDisplay: ")
        getErrorTextView()?.gone()
        getErrorTextView()?.text = ""
        getErrorView()?.gone()
    }

    override fun onDestroy() {
        Timber.d(tag, "onDestroy: ")
        if (apiCallingStatus == LOADING) {
            Timber.d(tag, "onDestroy: api is still loading, resetting to $INIT")
            apiCallingStatus = INIT
        }
        super.onDestroy()
    }
}