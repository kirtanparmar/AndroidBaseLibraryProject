package com.kirtan.mylibraryproject.ui.userList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.kirtan.mylibrary.base.activity.apiPagingList.MyApiListPagingListActivity
import com.kirtan.mylibrary.utils.parsedResponseForList.PageListParsedResponse
import com.kirtan.mylibraryproject.R
import com.kirtan.mylibraryproject.apis.Apis
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.User
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.UserListResponse
import com.kirtan.mylibraryproject.databinding.ActivityUserListBinding
import com.kirtan.mylibraryproject.ui.userInfo.UserInfoActivity
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

class UserListActivity :
    MyApiListPagingListActivity<ActivityUserListBinding, User, Int, UserListResponse>() {
    override fun getFirstPagePosition(): Int = 0
    override fun getRecyclerView(): RecyclerView = screen.rv
    override fun getCenterProgressBar(): ProgressBar = screen.centerProgress
    override fun getErrorTextView(): MaterialTextView = screen.errorMsg
    override fun getSwipeRefreshLayout(): SwipeRefreshLayout = screen.swipeRefresh
    override val getLayout: Int get() = R.layout.activity_user_list
    override fun storeBundleValueIfNeeded(bundle: Bundle) = true

    override fun getApiCallingFunction(apiRequest: Int): LiveData<Response<UserListResponse>?> =
        liveData(Dispatchers.IO) {
            val apiCalling = Apis.getInstance().getUsers(page = apiRequest)
            emit(apiCalling)
        }


    override fun parseListFromResponse(response: UserListResponse): LiveData<PageListParsedResponse<User>> =
        liveData(Dispatchers.Default) {
            if (response.users.isNotEmpty()) emit(
                PageListParsedResponse(
                    isSuccess = true,
                    newTotalItemCount = response.total,
                    newPageData = response.users
                )
            )
            else emit(PageListParsedResponse<User>(false, "$response"))
        }

    override fun createAdapter(): ListAdapter<User, *> =
        UserListAdapter() { model ->
            startActivity(Intent(this@UserListActivity, UserInfoActivity::class.java).also {
                it.putExtras(Bundle().apply { putParcelable("user", model) })
            })
        }

    override fun getLoaderDataModel(): User =
        User().also { it.isLoaderModel = true }

    override fun getApiRequest(): Int = getPage()
    override val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    override fun getBody(): View? = null
    override val emptyObjectForNullAssertion: User = User()
}