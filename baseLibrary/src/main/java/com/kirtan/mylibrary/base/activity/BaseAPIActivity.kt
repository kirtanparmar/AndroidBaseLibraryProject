package com.kirtan.mylibrary.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiCallingScreen
import com.kirtan.mylibrary.base.viewModels.ApiViewModel
import com.kirtan.mylibrary.utils.gone
import com.kirtan.mylibrary.utils.show
import com.kirtan.mylibrary.utils.toast

/**
 * This class should be used when you need to call the apis in your activity.
 */
abstract class BaseAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    BaseActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiViewModel: ApiViewModel<ApiResponseType> by viewModels()

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSwipeRefreshLayout()?.setOnRefreshListener { loadAPIData() }
        loadAPIData()
        observeApiResponse(apiViewModel.getResponseData())
    }

    /**
     * This function loads the data through the api.
     */
    private fun loadAPIData() {
        showPageProgress()
        getApiCallingFunction(getApiRequest()).observe(this) { response ->
            gonePageProgress()
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                val body = response.body()
                body?.let { apiViewModel.setResponseData(it) }
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
        if (getSwipeRefreshLayout()?.isRefreshing == true)
            getSwipeRefreshLayout()?.isRefreshing = false
        else getCenterProgressBar()?.gone()
        getBody()?.show()
    }
}