package com.example.greenleaf.data.repositories

import com.example.greenleaf.data.local.dao.UserDao
import com.example.greenleaf.data.local.entities.UserEntity
import com.example.greenleaf.data.remote.api.GreenLeafApi
import com.example.greenleaf.data.remote.models.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserDao,
    private val api: GreenLeafApi
) {
    suspend fun fetchAndCacheProfile() {
        val response = api.getUserProfile() // Returns Response<UserProfileResponse>
        if (response.isSuccessful) {
            val remoteProfile = response.body()?.toEntity() ?: return
            userProfileDao.insertProfile(remoteProfile)
        }
    }

    suspend fun getProfile(): UserEntity? = withContext(Dispatchers.IO) {
        userProfileDao.getProfile()
    }

    suspend fun updateProfile(profile: UserEntity) {
        userProfileDao.insertProfile(profile)
        // Optionally sync to backend if online
    }
}
