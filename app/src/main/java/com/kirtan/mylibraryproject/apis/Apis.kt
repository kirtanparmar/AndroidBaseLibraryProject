package com.kirtan.mylibraryproject.apis

import com.kirtan.mylibraryproject.apis.responseModels.userResponse.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface Apis {
    companion object {
        fun getInstance(): Apis = RetrofitHelper.retrofit.create(Apis::class.java)
    }

    @GET("user")
    suspend fun getUsers(
        @Header("app-id") appId: String = "YourAppId",
        @Query("page") page: Int
    ): Response<UserResponse>?
}