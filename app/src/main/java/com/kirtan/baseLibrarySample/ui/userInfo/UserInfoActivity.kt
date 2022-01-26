package com.kirtan.baseLibrarySample.ui.userInfo

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.kirtan.baseLibrary.base.activity.BaseAPIActivity
import com.kirtan.baseLibrarySample.R
import com.kirtan.baseLibrarySample.apis.UsersApi
import com.kirtan.baseLibrarySample.apis.responseModels.userInfoResponse.UserInfoResponse
import com.kirtan.baseLibrarySample.apis.responseModels.userListResponse.User
import com.kirtan.baseLibrarySample.databinding.ActivityUserInfoBinding
import retrofit2.Response

class UserInfoActivity : BaseAPIActivity<ActivityUserInfoBinding, String, UserInfoResponse>() {
    private var user: User = User()

    override suspend fun getApiCallingFunction(apiRequest: String): Response<UserInfoResponse>? =
        UsersApi.getInstance().getUserInfo(apiRequest)

    override fun getApiRequest(): String = user.id

    override fun observeApiDataResponse(apiResponse: LiveData<UserInfoResponse>) {
        apiResponse.observe(this) { response -> screen.model = response }
    }

    override val getLayout: Int get() = R.layout.activity_user_info

    override fun storeBundleValueIfNeeded(bundle: Bundle): Boolean {
        user = bundle.getParcelable("user") ?: User()
        return user.id.isNotBlank()
            .also { userIdAvailable ->
                if (userIdAvailable) screen.userName.text = "${user.firstName} ${user.lastName}"
            }
    }

    override fun getCenterProgressBar(): ProgressBar = screen.centerProgress
    override fun getBody(): View = screen.body
    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = screen.swipeRefresh
    override fun getErrorTextView(): MaterialTextView? = null
    override fun getErrorView(): View? = null
}