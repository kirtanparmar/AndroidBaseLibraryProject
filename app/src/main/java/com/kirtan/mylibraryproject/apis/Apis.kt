package com.kirtan.mylibraryproject.apis

import com.kirtan.mylibraryproject.apis.responseModels.userInfoResponse.UserInfoResponse
import com.kirtan.mylibraryproject.apis.responseModels.userListResponse.UserListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Apis {
    companion object {
        fun getInstance(): Apis = RetrofitHelper.retrofit.create(Apis::class.java)
    }

    @GET("user")
    suspend fun getUsers(@Query("page") page: Int): Response<UserListResponse>?

    @GET("user")
    suspend fun getUsers(): Response<UserListResponse>?

    @GET("user/{id}")
    suspend fun getUserInfo(@Path("id") id: String): Response<UserInfoResponse>?
}