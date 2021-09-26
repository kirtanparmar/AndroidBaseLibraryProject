package com.kirtan.mylibraryproject.apis

import com.google.gson.GsonBuilder
import com.kirtan.mylibraryproject.App
import com.kirtan.mylibraryproject.utils.Constants
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    private var retrofitInstance: Retrofit? = null

    var app: WeakReference<App> = WeakReference(null)

    fun resetClient() {
        retrofitInstance = null
    }

    val retrofit: Retrofit
        get() {
            if (retrofitInstance == null) {
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(Constants.BaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .build()
            }
            return retrofitInstance!!
        }

    private val okHttpClient: OkHttpClient
        get() = OkHttpClient()
            .newBuilder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val original = chain.request()

                    val request = original.newBuilder()
                        .method(original.method, original.body)
                        .build()

                    val response: Response
                    try {
                        response = chain.proceed(request)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Timber.d(e)
                        val body =
                            "Server Unreachable".toResponseBody(contentType = "text/plain".toMediaType())
                        return Response.Builder().code(Constants.CustomErrorCode)
                            .message("Server Unreachable")
                            .request(request)
                            .protocol(Protocol.HTTP_1_1).body(body).build()
                    }
                    if (response.code == 404) {
                        Timber.d("$request | $response")
                        val body =
                            "Server Unreachable".toResponseBody(contentType = "text/plain".toMediaType())
                        return Response.Builder().code(Constants.CustomErrorCode)
                            .message("Server Unreachable")
                            .request(request)
                            .protocol(Protocol.HTTP_1_1).body(body).build()
                    }
                    return response
                }
            })
            .apply {
                addInterceptor(HttpLoggingInterceptor { message ->
                    Timber.tag("OkHttp").d(message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            .connectTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .build()

}
