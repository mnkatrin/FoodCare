// auth/UserManager.kt
package com.example.foodcare.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext // <-- Добавь импорт
import kotlinx.coroutines.tasks.await
import javax.inject.Inject // <-- Добавь импорт
import javax.inject.Singleton // <-- Добавь импорт

// <-- Добавь аннотации
@Singleton
class UserManager @Inject constructor( // <-- Добавь @Inject к конструктору
    @ApplicationContext private val context: Context // <-- Убедись, что используешь @ApplicationContext
) {

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

    fun getCurrentUserId(): String {
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        if (savedUserId != null) {
            return savedUserId
        }

        val auth = FirebaseAuth.getInstance()
        val currentFirebaseUser = auth.currentUser

        if (currentFirebaseUser != null) {
            saveFirebaseUser(currentFirebaseUser)
            return currentFirebaseUser.uid
        }

        return createLocalUser()
    }

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

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

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

    suspend fun createUserWithFirebase(email: String, password: String, name: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
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

    fun getCurrentUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Пользователь") ?: "Пользователь"
    }

    fun getCurrentUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun isFirebaseUser(): Boolean {
        return prefs.getBoolean(KEY_IS_FIREBASE_USER, false)
    }

    fun isLocalUser(): Boolean {
        return !isFirebaseUser()
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun logout() {
        try {
            if (isFirebaseUser()) {
                auth.signOut()
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при выходе
        }
        prefs.edit().clear().apply()
        createLocalUser()
    }

    fun hasStoredSession(): Boolean {
        return prefs.contains(KEY_USER_ID)
    }

    fun getFirebaseUid(): String? {
        return prefs.getString(KEY_FIREBASE_UID, null)
    }

    /**
     * Возвращает true, если пользователь уже вошёл (не первый запуск и сессия сохранена),
     * иначе false. Это состояние, при котором можно, например, показать основной экран.
     */
    fun getLoginState(): Boolean {
        return !isFirstLaunch() && hasStoredSession()
    }
}