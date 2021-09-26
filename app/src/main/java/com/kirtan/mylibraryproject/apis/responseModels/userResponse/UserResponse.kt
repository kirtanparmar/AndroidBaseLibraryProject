package com.kirtan.mylibraryproject.apis.responseModels.userResponse

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("data")
    var users: ArrayList<User> = ArrayList(),
    @SerializedName("total")
    var total: Int = 0,
    @SerializedName("page")
    var page: Int = 0,
    @SerializedName("limit")
    var limit: Int = 0,
)