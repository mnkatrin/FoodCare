package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityLoginBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase Auth
        auth = Firebase.auth

        // Проверяем локально сохраненного пользователя
        checkCachedUser()

        setupClickListeners()
    }

    private fun checkCachedUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Пользователь уже авторизован локально - сразу переходим
            navigateToMain()
        }
    }

    private fun setupClickListeners() {
        // Кнопка "Войти"
        binding.button2.setOnClickListener {
            performLogin()
        }

        // Кнопка "Зарегистрироваться"
        binding.button3.setOnClickListener {
            navigateToRegister()
        }

        // Кнопка "Забыли пароль?"
        binding.button5.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        println("DEBUG: Attempting login with: $email")

        // Валидация полей
        if (email.isEmpty()) {
            showError("Введите email")
            return
        }

        if (password.isEmpty()) {
            showError("Введите пароль")
            return
        }

        // Блокируем кнопку на время авторизации
        binding.button2.isEnabled = false

        // Авторизация через Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.button2.isEnabled = true

                if (task.isSuccessful) {
                    println("DEBUG: Login successful, navigating to MainActivity")
                    showSuccess("Успешный вход!")
                    navigateToMain()
                } else {
                    println("DEBUG: Login failed: ${task.exception?.message}")
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("network", true) == true -> {
                // Нет интернета - проверяем локальный кэш
                checkOfflineAccess()
                return
            }
            exception?.message?.contains("password", true) == true ->
                "Неверный пароль"
            exception?.message?.contains("user-not-found", true) == true ->
                "Аккаунт не найден. Зарегистрируйтесь"
            exception?.message?.contains("invalid-email", true) == true ->
                "Неверный формат email"
            else -> "Ошибка авторизации: ${exception?.message}"
        }
        showError(errorMessage)
    }

    private fun checkOfflineAccess() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Есть локально сохраненный пользователь - разрешаем вход
            showSuccess("Оффлайн режим. Добро пожаловать!")
            navigateToMain()
        } else {
            // Нет локального пользователя и нет интернета
            showError("Нет подключения к интернету. Вход невозможен")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }
}