package com.kirtan.mylibraryproject.apis.responseModels.userInfoResponse

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("firstName")
    var firstName: String = "",
    @SerializedName("lastName")
    var lastName: String = "",
    @SerializedName("gender")
    var gender: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("dateOfBirth")
    var dateOfBirth: String = "",
    @SerializedName("registerDate")
    var registerDate: String = "",
    @SerializedName("phone")
    var phone: String = "",
    @SerializedName("picture")
    var picture: String = "",
    @SerializedName("location")
    var location: Location = Location(),
)