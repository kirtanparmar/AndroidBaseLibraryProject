package com.kirtan.mylibrary.base.fragment.apiList

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.kirtan.mylibrary.R
import com.kirtan.mylibrary.base.ApiListCallingScreen
import com.kirtan.mylibrary.base.dataHolder.BaseObject
import com.kirtan.mylibrary.base.fragment.BaseListFragment
import com.kirtan.mylibrary.base.viewModels.ApiCallingViewModel
import com.kirtan.mylibrary.utils.parsedResponseForList.ListParsedResponse
import com.kirtan.mylibrary.utils.toast
import timber.log.Timber

/**
 * Extend this class when you want to use the listing with api in your activity.
 */
abstract class BaseApiListFragment<Screen : ViewDataBinding, ModelType : BaseObject, ApiRequestType : Any?, ApiResponseType> :
    BaseListFragment<Screen, ModelType>(),
    ApiListCallingScreen<ApiRequestType, ApiResponseType, ListParsedResponse<ModelType>> {
    /**
     * This viewModel is auto implemented, no need to override this viewModel.
     */
    override val apiCallingViewModel: ApiCallingViewModel<ApiResponseType> by viewModels()

    /**
     * This function will auto load api data, you only have to parse the response.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPage()
        observeApiDataResponse(apiCallingViewModel.getResponseData())
    }

    /**
     * Function loads the data from api and calls the #observeApiResponse function on success.
     */
    private fun loadPage() {
        showPageProgress()
        getApiCallingFunction(getApiRequest()).observe(viewLifecycleOwner) { response ->
            Timber.d("$response")
            gonePageProgress()
            if (response == null) {
                toast(getString(R.string.server_unreachable))
                return@observe
            }
            if (response.isSuccessful) {
                Timber.d("${response.body()}")
                val body = response.body()
                if (body == null) {
                    toast(response.message())
                    return@observe
                }
                apiCallingViewModel.setResponseData(body)
            } else {
                Timber.d("${response.code()} ${response.message()}")
                toast(response.message())
            }
        }
    }

    /**
     * Function will observe the api response and will request for parsing the response into the list of models to be listed in the list screen.
     */
    override fun observeApiDataResponse(apiResponse: LiveData<ApiResponseType>) {
        apiResponse.observe(viewLifecycleOwner) { responseBody ->
            parseListFromResponse(responseBody).observe(viewLifecycleOwner) { parsedResponse ->
                if (parsedResponse.isSuccess)
                    getRecyclerView().post { models.addAll(parsedResponse.apiListData) }
                else showErrorOnDisplay(parsedResponse.errorMessage)
            }
        }
    }

    /**
     * Function will display the loader according the need.
     */
    override fun showPageProgress() {
        if (getSwipeRefreshLayout()?.isRefreshing == true) return
        super.showPageProgress()
    }

    /**
     * Function will hide the loader according the need.
     */
    override fun gonePageProgress() {
        if (getSwipeRefreshLayout()?.isRefreshing == true) {
            getSwipeRefreshLayout()?.isRefreshing = false
            return
        }
        super.gonePageProgress()
    }

    /**
     * Function will set SwipeToRefresh layout.
     */
    override fun setSwipeRefresh() {
        getSwipeRefreshLayout()?.setOnRefreshListener {
            hideErrorOnDisplay()
            models.clear()
            loadPage()
        }
    }
}