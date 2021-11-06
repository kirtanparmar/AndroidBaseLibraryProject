package com.kirtan.mylibrary.base.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiCallingScreen
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.mylibrary.utils.toast

abstract class BaseAPIFragment<Screen : ViewDataBinding, ApiRequestType : Any?, ApiResponseType> :
    BaseFragment<Screen>(), ApiCallingScreen<ApiRequestType, ApiResponseType> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()

    /**
     * This method should be overridden whenever you needed to set up screen though code.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadAPIData()
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    /**
     * This function loads the data through the api.
     */
    private fun loadAPIData() {
        getApiCallingFunction(getApiRequest()).observe(viewLifecycleOwner) { response ->
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                val body = response.body()
                body?.let { apiCallingViewModel.setResponseData(it) }
            } else toast(response.message())
        }
    }
}