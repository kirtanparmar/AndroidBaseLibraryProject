package com.kirtan.mylibrary.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiCallingScreen
import com.kirtan.mylibrary.base.viewModels.ApiViewModel
import com.kirtan.mylibrary.utils.toast

/**
 * This class should be used when you need to call the apis in your activity.
 */
abstract class MyAPIActivity<Screen : ViewDataBinding, ApiRequest : Any?, ApiResponseType> :
    MyActivity<Screen>(), ApiCallingScreen<ApiRequest, ApiResponseType> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiViewModel: ApiViewModel<ApiResponseType> by viewModels()

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAPIData()
        observeApiResponse(apiViewModel.getResponseData())
    }

    /**
     * This function loads the data through the api.
     */
    private fun loadAPIData() {
        getApiCallingFunction(getApiRequest()).observe(this) { response ->
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
}