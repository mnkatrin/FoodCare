// auth/UserManager.kt
package com.example.foodcare.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class UserManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_FIREBASE_UID = "firebase_uid"
        private const val KEY_IS_FIREBASE_USER = "is_firebase_user"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }

    // ОСНОВНОЙ МЕТОД - всегда возвращает ID пользователя
// ОСНОВНОЙ МЕТОД - всегда возвращает ID пользователя
    fun getCurrentUserId(): String {
        // Если есть сохраненный ID - возвращаем его
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        if (savedUserId != null) {
            return savedUserId
        }

        // Проверяем, не авторизован ли пользователь через Firebase
        val auth = FirebaseAuth.getInstance()
        val currentFirebaseUser = auth.currentUser

        if (currentFirebaseUser != null) {
            // Если Firebase помнит пользователя - сохраняем его данные
            saveFirebaseUser(currentFirebaseUser)
            return currentFirebaseUser.uid
        }

        // Только если нет никаких следов авторизации - создаем локального пользователя
        return createLocalUser()
    }

    // АВТОМАТИЧЕСКОЕ СОЗДАНИЕ ЛОКАЛЬНОГО ПОЛЬЗОВАТЕЛЯ ПРИ ПЕРВОМ ЗАПУСКЕ
    private fun createLocalUser(): String {
        val localUserId = "local_user_${System.currentTimeMillis()}_${(1000..9999).random()}"

        prefs.edit().apply {
            putString(KEY_USER_ID, localUserId)
            putString(KEY_USER_NAME, "Пользователь")
            putString(KEY_USER_EMAIL, "")
            putBoolean(KEY_IS_FIREBASE_USER, false)
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }.apply()

        return localUserId
    }

    // ПРОВЕРКА - ПЕРВЫЙ ЛИ ЭТО ЗАПУСК
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    // FIREBASE АВТОРИЗАЦИЯ (опционально)
    suspend fun signInWithFirebase(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                saveFirebaseUser(user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // FIREBASE РЕГИСТРАЦИЯ (опционально)
    suspend fun createUserWithFirebase(email: String, password: String, name: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Обновляем display name в Firebase
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                saveFirebaseUser(user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // СОХРАНЕНИЕ ДАННЫХ FIREBASE ПОЛЬЗОВАТЕЛЯ
    private fun saveFirebaseUser(user: FirebaseUser) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.uid)
            putString(KEY_FIREBASE_UID, user.uid)
            putString(KEY_USER_NAME, user.displayName ?: "Пользователь")
            putString(KEY_USER_EMAIL, user.email ?: "")
            putBoolean(KEY_IS_FIREBASE_USER, true)
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }.apply()
    }

    // ПОЛУЧЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ
    fun getCurrentUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Пользователь") ?: "Пользователь"
    }

    fun getCurrentUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    // ПРОВЕРКА ТИПА ПОЛЬЗОВАТЕЛЯ
    fun isFirebaseUser(): Boolean {
        return prefs.getBoolean(KEY_IS_FIREBASE_USER, false)
    }

    fun isLocalUser(): Boolean {
        return !isFirebaseUser()
    }

    // ОБНОВЛЕНИЕ ДАННЫХ
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    // ВЫХОД (очищает ВСЕ данные)
    fun logout() {
        try {
            if (isFirebaseUser()) {
                auth.signOut()
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при выходе
        }

        // Очищаем ВСЕ данные
        prefs.edit().clear().apply()

        // Создаем нового локального пользователя
        createLocalUser()
    }

    // ПРОВЕРКА - ЕСТЬ ЛИ СОХРАНЕННАЯ СЕССИЯ
    fun hasStoredSession(): Boolean {
        return prefs.contains(KEY_USER_ID)
    }

    // ПОЛУЧЕНИЕ FIREBASE UID (если есть)
    fun getFirebaseUid(): String? {
        return prefs.getString(KEY_FIREBASE_UID, null)
    }


}