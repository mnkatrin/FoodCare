package com.example.foodcare.ui.splash

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.foodcare.FoodCareApplication
import com.example.foodcare.databinding.ActivitySplashBinding
import com.example.foodcare.ui.auth.LoginActivity
import com.example.foodcare.ui.base.FullScreenActivity
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : FullScreenActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        Log.d(TAG, "Splash activity started")

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 1500)
    }

    private fun checkAuthAndNavigate() {
        Log.d(TAG, "Checking authentication status")

        // Получаем UserManager
        val userManager = (application as FoodCareApplication).userManager

        // --- ИСПРАВЛЕНО: Вызов через UserManager ---
        val isLoggedIn = userManager.getLoginState() // <-- Возвращает Boolean
        val savedEmail = userManager.getCurrentUserEmail() // <-- Получаем email от UserManager
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        Log.d(TAG, "App login state: isLoggedIn=$isLoggedIn, email=$savedEmail")

        // Проверяем UserManager состояние
        val userManagerEmail = userManager.getCurrentUserEmail()
        val isFirebaseUser = userManager.isFirebaseUser()
        Log.d(TAG, "UserManager state: email=$userManagerEmail, isFirebaseUser=$isFirebaseUser")

        // Check Firebase auth
        val auth = Firebase.auth
        val currentUser = auth.currentUser
        Log.d(TAG, "Firebase user: $currentUser")

        // КРИТЕРИЙ ПЕРЕХОДА НА ГЛАВНЫЙ ЭКРАН:
        // 1. Есть сохраненное состояние в UserManager И (isLoggedIn == true) ИЛИ
        // 2. UserManager говорит что это Firebase пользователь ИЛИ
        // 3. Firebase Auth имеет текущего пользователя
        val shouldNavigateToMain = (isLoggedIn && savedEmail.isNotEmpty()) || // <-- Уточнение: isLoggedIn == true и email есть
                isFirebaseUser ||
                currentUser != null

        if (shouldNavigateToMain) {
            Log.d(TAG, "Navigating to MainActivity")

            // Синхронизируем состояния если нужно
            if (isLoggedIn && savedEmail.isNotEmpty() && userManagerEmail != savedEmail) {
                userManager.setUserEmail(savedEmail)
            }

            navigateToMain()
        } else {
            Log.d(TAG, "Navigating to LoginActivity")
            navigateToLogin()
        }
    }

    private fun attemptAutoLogin(savedEmail: String) {
        Log.d(TAG, "Attempting auto login for: $savedEmail")
        // Here you can add auto login logic if needed
        // For now just navigate to login
        navigateToLogin()
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