package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.foodcare.ui.base.FullScreenActivity

class ForgotPasswordActivity : FullScreenActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase Auth
        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка "Назад"
        binding.buttonback.setOnClickListener {
            finish() // Возвращаемся к предыдущему экрану (LoginActivity)
        }

        // Кнопка "Запросить сброс"
        binding.buttonReset.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        // Получаем email из TextInputEditText внутри TextInputLayout
        val email = binding.etEmailReset.text.toString().trim()

        // Валидация email
        if (email.isEmpty()) {
            showError("Введите email")
            binding.etEmailReset.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            binding.etEmailReset.requestFocus()
            return
        }

        // Блокируем кнопку на время отправки
        binding.buttonReset.isEnabled = false

        // Отправка письма для сброса пароля
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.buttonReset.isEnabled = true

                if (task.isSuccessful) {
                    // Успешная отправка
                    showSuccess("Ссылка для сброса пароля отправлена на ваш email")
                    // Возвращаемся на экран авторизации через 2 секунды
                    binding.root.postDelayed({
                        finish()
                    }, 2000)
                } else {
                    // Ошибка отправки
                    handleResetError(task.exception?.message)
                }
            }
    }

    private fun handleResetError(errorMessage: String?) {
        val message = when {
            errorMessage?.contains("network", true) == true ->
                "Проверьте подключение к интернету"
            errorMessage?.contains("user not found", true) == true ->
                "Пользователь с таким email не найден"
            errorMessage?.contains("invalid email", true) == true ->
                "Неверный формат email"
            else -> "Ошибка: ${errorMessage ?: "Неизвестная ошибка"}"
        }
        showError(message)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}