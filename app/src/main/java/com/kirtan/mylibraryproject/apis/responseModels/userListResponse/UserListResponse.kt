package com.kirtan.mylibraryproject.apis.responseModels.userListResponse

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("data")
    var users: ArrayList<User> = ArrayList(),
    @SerializedName("total")
    var total: Int = 0,
    @SerializedName("page")
    var page: Int = 0,
    @SerializedName("limit")
    var limit: Int = 0,
)