package com.kirtan.mylibraryproject.ui.userInfo

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kirtan.mylibrary.base.activity.MyAPIActivity
import com.kirtan.mylibraryproject.R
import com.kirtan.mylibraryproject.apis.Apis
import com.kirtan.mylibraryproject.apis.responseModels.userInfoResponse.UserInfoResponse
import com.kirtan.mylibraryproject.databinding.ActivityUserInfoBinding
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

class UserInfoActivity : MyAPIActivity<ActivityUserInfoBinding, String, UserInfoResponse>() {
    private var userId: String = ""

    override fun getApiCallingFunction(apiRequest: String): LiveData<Response<UserInfoResponse>?> =
        liveData(Dispatchers.IO) {
            val apiCalling = Apis.getInstance().getUserInfo(apiRequest)
            emit(apiCalling)
        }

    override fun getApiRequest(): String = userId

    override fun observeApiResponse(apiResponse: LiveData<UserInfoResponse>) {
        apiResponse.observe(this) { response -> screen.model = response }
    }

    override val getLayout: Int get() = R.layout.activity_user_info

    override fun storeBundleValueIfNeeded(bundle: Bundle): Boolean {
        userId = bundle.getString("id", "")
        return userId.isNotBlank()
    }

    override fun getCenterProgressBar(): ProgressBar = screen.centerProgress
    override fun getBody(): View = screen.body
    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = screen.swipeRefresh
}