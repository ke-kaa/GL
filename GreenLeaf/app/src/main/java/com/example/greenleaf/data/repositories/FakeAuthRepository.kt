package com.example.greenleaf.data.repositories

import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.interceptors.AuthInterceptor
import com.example.greenleaf.data.remote.models.LoginRequest
import com.example.greenleaf.domain.repositories.AuthRepository
import com.example.greenleaf.domain.valueobjects.Email
import com.example.greenleaf.domain.valueobjects.Password
import com.example.greenleaf.domain.common.ResultOrFailure
import com.example.greenleaf.domain.failures.DomainFailure
import javax.inject.Inject

//class AuthRepositoryImpl @Inject constructor() : AuthRepository {
//    private val mockEmail = "test@example.com"
//    private val mockPassword = "password123"
//
//    override suspend fun login(email: Email, password: Password): ResultOrFailure<Unit> {
//        return if (email.value == mockEmail && password.value == mockPassword) {
//            ResultOrFailure.Success(Unit)
//        } else {
//            ResultOrFailure.Failure(DomainFailure.Unauthorized("Invalid email or password"))
//        }
//    }
//}

class AuthRepositoryImpl @Inject constructor(
    private val api: GreenLeafApi,
    private val authInterceptor: AuthInterceptor
) : AuthRepository {

    override suspend fun login(email: Email, password: Password): ResultOrFailure<Unit> {
        return try {
            val response = api.login(LoginRequest(email.value, password.value))

            if (response.isSuccessful) {
                val body = response.body()
                // Store tokens in interceptor
                authInterceptor.setTokens(body?.access, body?.refresh)
                ResultOrFailure.Success(Unit)
            } else {
                ResultOrFailure.Failure(DomainFailure.Unauthorized("Invalid email or password"))
            }
        } catch (e: Exception) {
            ResultOrFailure.Failure(DomainFailure.NetworkError("Network connection issue"))
        }
    }
} 