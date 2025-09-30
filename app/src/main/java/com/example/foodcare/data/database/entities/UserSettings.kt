package com.example.data.database.entities

import androidx.room.*

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val user_id: String,

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true,

    @ColumnInfo(name = "expiry_notification_days")
    val expiryNotificationDays: Int = 3,

    @ColumnInfo(name = "recipes_notification_enabled")
    val recipesNotificationEnabled: Boolean = true,

    @ColumnInfo(name = "notification_time")
    val notificationTime: String = "09:00",

    val theme: String = "system"
)