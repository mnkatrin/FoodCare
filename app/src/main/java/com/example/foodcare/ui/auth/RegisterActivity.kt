package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityRegisterBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка "Зарегистрироваться"
        binding.button.setOnClickListener {
            performRegistration()
        }

        // Кнопка "Назад"
        binding.imageButton3.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val email = binding.etEmail2.text.toString().trim()
        val password = binding.etPassword2.text.toString().trim()

        // Валидация полей
        if (email.isEmpty()) {
            showError("Введите email")
            return
        }

        if (password.isEmpty()) {
            showError("Введите пароль")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            return
        }

        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            return
        }

        // Блокируем кнопку на время регистрации
        binding.button.isEnabled = false

        // Регистрация через Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.button.isEnabled = true

                if (task.isSuccessful) {
                    // Успешная регистрация
                    showSuccess("Регистрация успешна!")
                    navigateToMain()
                } else {
                    // Ошибка регистрации
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("network", true) == true ->
                "Проверьте подключение к интернету"
            exception?.message?.contains("email-already-in-use", true) == true ->
                "Этот email уже зарегистрирован. Войдите в аккаунт"
            exception?.message?.contains("invalid-email", true) == true ->
                "Неверный формат email"
            exception?.message?.contains("weak-password", true) == true ->
                "Пароль слишком слабый. Используйте минимум 6 символов"
            exception?.message?.contains("operation-not-allowed", true) == true ->
                "Регистрация отключена. Обратитесь к администратору"
            else -> "Ошибка регистрации: ${exception?.message}"
        }
        showError(errorMessage)
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
        finishAffinity() // Закрываем всю цепочку авторизации
    }
}