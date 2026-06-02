package com.mssde.mobilelantern.data.remote

import com.mssde.mobilelantern.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url: HttpUrl = originalRequest.url

        val isCheckIn = url.encodedPath.contains("/ia/checkin")
        val alreadyHasToken = originalRequest.header("Token") != null

        val token = tokenManager.getToken()
        val requestBuilder = originalRequest.newBuilder()
        if (!isCheckIn && !alreadyHasToken) {
            token?.let { requestBuilder.addHeader("Token", it) }
        }
        return chain.proceed(requestBuilder.build())
    }
} 