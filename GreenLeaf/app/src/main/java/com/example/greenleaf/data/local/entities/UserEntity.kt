package com.example.greenleaf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val firstName: String?,
    val lastName: String?,
    val birthdate: String?,
    val gender: String?,
    val email: String,
    val phoneNumber: String?,
    val profileImage: String?,
) 