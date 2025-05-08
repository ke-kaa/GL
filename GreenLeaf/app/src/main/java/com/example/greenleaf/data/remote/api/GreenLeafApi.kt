package com.example.greenleaf.data.remote.api

import com.example.greenleaf.data.remote.models.LoginRequest
import com.example.greenleaf.data.remote.models.LoginResponse
import com.example.greenleaf.data.remote.models.ObservationRequest
import com.example.greenleaf.data.remote.models.ObservationResponse
import com.example.greenleaf.data.remote.models.PlantRequest
import com.example.greenleaf.data.remote.models.PlantResponse
import com.example.greenleaf.data.remote.models.RegisterRequest
import com.example.greenleaf.data.remote.models.RegisterResponse
import com.example.greenleaf.data.remote.models.UpdateProfileRequest
import com.example.greenleaf.data.remote.models.UserProfileResponse
import com.example.greenleaf.data.remote.models.UserStatsResponse
import com.example.greenleaf.data.remote.models.UserLogoutReponse
import com.example.greenleaf.data.remote.models.LogoutRequest
import retrofit2.Response
import retrofit2.http.*

interface GreenLeafApi {
    // User endpoints
    @POST("account/api/token/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("account/api/register/")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @GET("account/api/profile/")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @PATCH("account/api/profile/")
    suspend fun updateUserProfile(@Body profileRequest: UpdateProfileRequest): Response<UserProfileResponse>

    @POST("account/api/logout/")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<Unit>

    @DELETE("account/api/profile/")
    suspend fun deleteAccount(): Response<Unit>

    // Plant endpoints
    @GET("api/plants/")
    suspend fun getPlants(): Response<List<PlantResponse>>

    @GET("api/plants/{id}/")
    suspend fun getPlantDetail(@Path("id") id: Int): Response<PlantResponse>

    @POST("api/plants/")
    suspend fun createPlant(@Body plantRequest: PlantRequest): Response<PlantResponse>

    @PUT("api/plants/{id}/")
    suspend fun updatePlant(
        @Path("id") id: Int,
        @Body plantRequest: PlantRequest
    ): Response<PlantResponse>

    @DELETE("api/plants/{id}/")
    suspend fun deletePlant(@Path("id") id: Int): Response<Unit>

    // Observation endpoints
    @GET("api/observations/")
    suspend fun getObservations(): Response<List<ObservationResponse>>

    @GET("api/observations/{id}/")
    suspend fun getObservationDetail(@Path("id") id: Int): Response<ObservationResponse>

    @POST("api/observations/")
    suspend fun createObservation(@Body observationRequest: ObservationRequest): Response<ObservationResponse>

    @PUT("api/observations/{id}/")
    suspend fun updateObservation(
        @Path("id") id: Int,
        @Body observationRequest: ObservationRequest
    ): Response<ObservationResponse>

    @DELETE("api/observations/{id}/")
    suspend fun deleteObservation(@Path("id") id: Int): Response<Unit>

    // Admin endpoints
    @GET("account/api/users/list/")
    suspend fun getAllUsers(): Response<List<UserStatsResponse>>
}