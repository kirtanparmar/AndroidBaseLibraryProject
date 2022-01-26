package com.kirtan.baseLibrarySample.ui.userList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.kirtan.baseLibrary.base.activity.apiPagingList.BaseApiListPagingListActivity
import com.kirtan.baseLibrary.base.activity.apiPagingList.PaginationOn
import com.kirtan.baseLibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.baseLibrarySample.R
import com.kirtan.baseLibrarySample.apis.UsersApi
import com.kirtan.baseLibrarySample.apis.responseModels.userListResponse.User
import com.kirtan.baseLibrarySample.apis.responseModels.userListResponse.UserListResponse
import com.kirtan.baseLibrarySample.databinding.ActivityUserListBinding
import com.kirtan.baseLibrarySample.ui.userInfo.UserInfoActivity
import retrofit2.Response

class UserListPagingActivity :
    BaseApiListPagingListActivity<ActivityUserListBinding, User, Int, UserListResponse>() {
    override fun getFirstPagePosition(): Int = 0
    override fun getRecyclerView(): RecyclerView = screen.rv
    override fun getCenterProgressBar(): ProgressBar = screen.centerProgress
    override fun getErrorTextView(): MaterialTextView = screen.errorMsg
    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = screen.swipeRefresh
    override val getLayout: Int get() = R.layout.activity_user_list
    override fun bundleFromPreviousActivity(bundle: Bundle) = true

    override suspend fun getApiCallingFunction(apiRequest: Int): Response<UserListResponse>? =
        UsersApi.getInstance().getUsers(page = apiRequest)

    override fun parseListFromResponse(response: UserListResponse): PageListParsedResponse<User> =
        if (response.users.isNotEmpty())
            PageListParsedResponse(
                isSuccess = true,
                newTotalPagination = response.total,
                newPageData = response.users
            )
        else PageListParsedResponse(false, "$response")

    override fun createAdapter(): ListAdapter<User, *> =
        UserListAdapter() { model ->
            startActivity(Intent(this@UserListPagingActivity, UserInfoActivity::class.java).also {
                it.putExtras(Bundle().apply { putParcelable("user", model) })
            })
        }

    override fun getLoaderDataModel(): User =
        User().also { it.isLoaderModel = true }

    override fun getApiRequest(): Int = getCurrentPage()
    override val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    override fun getBody(): View? = null
    override val emptyObjectForNullAssertion: User = User()
    override fun getErrorView(): View? = null
    override fun getPaginationType(): PaginationOn = PaginationOn.ITEM_COUNT
}