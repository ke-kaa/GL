package com.example.greenleaf.data.remote.models

import com.example.greenleaf.data.local.entities.UserEntity
import com.google.gson.annotations.SerializedName

// Correct
data class LoginRequest(
    val email: String,
    val password: String
)

// Correct
data class LoginResponse(
    val access: String,
    val refresh: String
)

// Correct
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

// Correct
data class RegisterResponse(
    val user: UserProfileResponse,
    val access: String,
    val refresh: String
)

// Correct
data class UserProfileResponse(
    val id: Int,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val birthdate: String?,
    val gender: String?,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("profile_image") val profileImage: String?
)

data class UpdateProfileRequest(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val birthdate: String?,
    val gender: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("profile_image") val profileImage: String?  // URL or base64-encoded string for the new profile image
)

data class UserStatsResponse(
    val id: Int,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val email: String,
    @SerializedName("total_plant_record") val plantCount: Int,
    @SerializedName("total_observation_records") val observationCount: Int,
    @SerializedName("is_admin") val isAdmin: Boolean
)

data class UserLogoutReponse (
    val message: String
)

data class LogoutRequest(
    val refresh: String
)

fun UserProfileResponse.toEntity(): UserEntity = UserEntity(
    id = this.id,
    firstName = this.firstName,
    lastName = this.lastName,
    birthdate = this.birthdate,
    gender = this.gender,
    email = this.email,
    phoneNumber = this.phoneNumber,
    profileImage = this.profileImage
)
