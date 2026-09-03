package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val name: String,
    val avatarUrl: String?,
    val createdAt: Long
)
