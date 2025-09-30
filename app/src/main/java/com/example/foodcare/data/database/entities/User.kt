package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val user_id: String,
    val email: String,
    val name: String?,
    val profile_picture_url: String?,
    val created_at: Long = System.currentTimeMillis()
)