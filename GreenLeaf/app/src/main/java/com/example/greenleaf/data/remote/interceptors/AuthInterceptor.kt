package com.example.greenleaf.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun setTokens(access: String?, refresh: String?) {
        this.accessToken = access
        this.refreshToken = refresh
    }

    fun getRefreshToken(): String? = refreshToken

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login and register endpoints
        if (originalRequest.url.encodedPath.contains("account/api/token/") ||
            originalRequest.url.encodedPath.contains("account/api/register/")) {
            return chain.proceed(originalRequest)
        }

        // Add token to authenticated requests
        val authenticatedRequest = accessToken?.let { token ->
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } ?: originalRequest

        return chain.proceed(authenticatedRequest)
    }
}