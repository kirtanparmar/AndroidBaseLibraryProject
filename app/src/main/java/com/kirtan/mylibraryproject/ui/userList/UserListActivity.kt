package com.kirtan.mylibraryproject.ui.userList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.kirtan.baseLibrary.base.activity.apiList.BaseApiListActivity
import com.kirtan.baseLibrary.utils.parsedResponseForList.ListParsedResponse
import com.kirtan.mylibraryproject.R
import com.kirtan.mylibraryproject.apis.Apis
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.User
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.UserListResponse
import com.kirtan.mylibraryproject.databinding.ActivityUserListBinding
import com.kirtan.mylibraryproject.ui.userInfo.UserInfoActivity
import retrofit2.Response

class UserListActivity :
    BaseApiListActivity<ActivityUserListBinding, User, Int, UserListResponse>() {
    override fun getRecyclerView(): RecyclerView = screen.rv
    override fun getCenterProgressBar(): ProgressBar = screen.centerProgress
    override fun getErrorTextView(): MaterialTextView = screen.errorMsg
    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = screen.swipeRefresh
    override val getLayout: Int get() = R.layout.activity_user_list
    override fun storeBundleValueIfNeeded(bundle: Bundle) = true

    override suspend fun getApiCallingFunction(apiRequest: Int): Response<UserListResponse>? =
        Apis.getInstance().getUsers()

    override fun parseListFromResponse(response: UserListResponse): ListParsedResponse<User> =
        if (response.users.isNotEmpty())
            ListParsedResponse(
                isSuccess = true,
                apiListData = response.users
            )
        else ListParsedResponse(false, "$response")

    override fun createAdapter(): ListAdapter<User, *> =
        UserListAdapter() { model ->
            startActivity(Intent(this@UserListActivity, UserInfoActivity::class.java).also {
                it.putExtras(Bundle().apply { putParcelable("user", model) })
            })
        }

    override val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    override fun getBody(): View? = null
    override val emptyObjectForNullAssertion: User = User()
    override fun getApiRequest(): Int {
        return 0
    }

    override fun getErrorView(): View? = null
}