package com.kirtan.baseLibrarySample.apis.responseModels.userListResponse

import com.google.gson.annotations.SerializedName
import com.kirtan.baseLibrary.base.activity.apiPagingList.PagingApiResponse

data class UserListResponse(
    @SerializedName("data")
    var users: ArrayList<User> = ArrayList(),
    @SerializedName("total")
    var total: Int = 0,
    @SerializedName("page")
    var page: Int = 0,
    @SerializedName("limit")
    var limit: Int = 0,
) : PagingApiResponse()