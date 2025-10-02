package com.example.foodcare.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.foodcare.databinding.ActivitySplashBinding
import com.example.foodcare.ui.auth.LoginActivity
import com.example.foodcare.ui.base.FullScreenActivity
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : FullScreenActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Автоматическая проверка пользователя через 2 секунды
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 2000)
    }

    private fun checkUserAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Пользователь есть локально - проверяем его валидность на сервере
            verifyUserOnServer(currentUser.uid)
        } else {
            // Пользователя нет локально - на экран логина
            navigateToLogin()
        }
    }

    private fun verifyUserOnServer(userId: String) {
        // Принудительно обновляем токен - если пользователь удален, это вызовет ошибку
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Токен обновлен - пользователь существует на сервере
                navigateToMain()
            } else {
                // Не удалось обновить токен - пользователь удален или невалиден
                when {
                    task.exception is FirebaseAuthInvalidUserException -> {
                        // Пользователь удален, отключен или не существует
                        handleUserInvalid()
                    }
                    else -> {
                        // Другие ошибки (сеть и т.д.) - все равно выходим для безопасности
                        handleUserInvalid()
                    }
                }
            }
        }?.addOnFailureListener {
            // При любой ошибке считаем пользователя невалидным
            handleUserInvalid()
        }
    }

    private fun handleUserInvalid() {
        // Выходим и очищаем данные
        auth.signOut()
        clearLocalData()

        Toast.makeText(this, "Сессия истекла. Войдите снова", Toast.LENGTH_LONG).show()
        navigateToLogin()
    }

    private fun clearLocalData() {
        // Очищаем SharedPreferences и другие локальные данные
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Здесь можно добавить очистку локальной БД если используется Room/SQLite
        // database.clearAllTables()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}