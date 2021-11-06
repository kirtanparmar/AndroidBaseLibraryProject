package com.kirtan.mylibrary.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiCallingScreen
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel.ApiStatus.*
import com.kirtan.mylibrary.utils.gone
import com.kirtan.mylibrary.utils.show
import com.kirtan.mylibrary.utils.toast
import timber.log.Timber

/**
 * This class should be used when you need to call the apis in your activity.
 */
abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    val tag = "BaseAPIActivity"

    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()
    private var apiCallingStatus: ApiCallingViewModel.ApiStatus?
        get() = apiCallingViewModel.status.value
        set(value) {
            apiCallingViewModel.status.value = value
        }

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSwipeRefreshLayout()?.setOnRefreshListener { loadAPIData() }
        apiCallingViewModel.status.observe(this) { status ->
            when (status) {
                INIT -> loadAPIData()
                COMPLETE -> gonePageProgress()
                LOADING -> showPageProgress()
                null -> {
                    apiCallingStatus = INIT
                    Timber.d("Found null type of api status.")
                }
            }
        }
        observeApiResponse(apiCallingViewModel.getResponseData())
    }

    /**
     * This function loads the data through the api.
     */
    private fun loadAPIData() {
        if (apiCallingStatus == LOADING) return
        apiCallingStatus = LOADING
        getApiCallingFunction(getApiRequest()).observe(this) { response ->
            apiCallingStatus = COMPLETE
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    apiCallingViewModel.setResponseData(it)
                    apiCallingViewModel.status.value = COMPLETE
                }
            } else {
                toast(response.message())
            }
        }
    }

    /**
     * Function will display the loader according the need.
     */
    private fun showPageProgress() {
        getBody()?.gone()
        if (getSwipeRefreshLayout()?.isRefreshing == true) return
        getCenterProgressBar()?.show()
    }

    /**
     * Function will hide the loader according the need.
     */
    private fun gonePageProgress() {
        getSwipeRefreshLayout()?.post { getSwipeRefreshLayout()?.isRefreshing = false }
        getCenterProgressBar()?.gone()
        getBody()?.show()
    }

    override fun onDestroy() {
        if (apiCallingStatus == LOADING) apiCallingStatus = INIT
        super.onDestroy()
    }
}