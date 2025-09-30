package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entities.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT COUNT(*) FROM users WHERE user_id = :userId")
    suspend fun userExists(userId: String): Int
}