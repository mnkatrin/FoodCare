package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityLoginBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.button2.setOnClickListener {
            performLogin()
        }

        binding.button3.setOnClickListener {
            navigateToRegister()
        }

        binding.button5.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Валидация полей
        if (email.isEmpty()) {
            showError("Введите email или номер телефона")
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            showError("Введите пароль")
            binding.etPassword.requestFocus()
            return
        }

        // Блокируем кнопку на время авторизации
        binding.button2.isEnabled = false
        binding.button2.text = "Вход..."

        // Определяем тип ввода (email или телефон)
        val inputType = determineInputType(email)
        if (inputType == InputType.UNKNOWN) {
            showError("Введите корректный email или номер телефона")
            binding.etEmail.requestFocus()
            binding.button2.isEnabled = true
            binding.button2.text = "Войти"
            return
        }

        // Пытаемся войти
        attemptLogin(email, password, inputType)
    }

    private fun determineInputType(input: String): InputType {
        return when {
            android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() -> InputType.EMAIL
            isValidPhoneNumber(input) -> InputType.PHONE
            else -> InputType.UNKNOWN
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        return when {
            cleanPhone.length !in 10..12 -> false
            cleanPhone.startsWith("7") || cleanPhone.startsWith("8") ||
                    cleanPhone.startsWith("9") && cleanPhone.length == 11 -> true
            cleanPhone.length == 10 -> true
            else -> false
        }
    }

    private fun attemptLogin(emailOrPhone: String, password: String, inputType: InputType) {
        val loginEmail = if (inputType == InputType.PHONE) {
            // Для телефона создаем временный email
            val formattedPhone = formatPhoneNumber(emailOrPhone)
            "${formattedPhone.replace("[^0-9]".toRegex(), "")}@foodcare.com"
        } else {
            emailOrPhone
        }

        auth.signInWithEmailAndPassword(loginEmail, password)
            .addOnCompleteListener(this) { task ->
                binding.button2.isEnabled = true
                binding.button2.text = "Войти"

                if (task.isSuccessful) {
                    // Успешный вход
                    handleSuccessfulLogin()
                } else {
                    // Ошибка входа
                    handleLoginError(task.exception, emailOrPhone, password, inputType)
                }
            }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        return when {
            cleanPhone.length == 10 -> "+7$cleanPhone"
            cleanPhone.startsWith("7") && cleanPhone.length == 11 -> "+$cleanPhone"
            cleanPhone.startsWith("8") && cleanPhone.length == 11 -> "+7${cleanPhone.substring(1)}"
            cleanPhone.startsWith("9") && cleanPhone.length == 11 -> "+7$cleanPhone"
            else -> "+$cleanPhone"
        }
    }

    private fun handleSuccessfulLogin() {
        showSuccess("Успешный вход!")
        navigateToMain()
    }

    private fun handleLoginError(exception: Exception?, emailOrPhone: String, password: String, inputType: InputType) {
        when {
            exception is FirebaseAuthInvalidUserException -> {
                // Пользователь не существует, удален или отключен
                when (inputType) {
                    InputType.EMAIL -> showError("Пользователь с таким email не существует")
                    InputType.PHONE -> showError("Пользователь с таким номером телефона не существует")
                    else -> showError("Пользователь не существует")
                }
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("user-not-found", true) == true -> {
                // Пользователь не найден
                when (inputType) {
                    InputType.EMAIL -> showError("Пользователь с таким email не зарегистрирован")
                    InputType.PHONE -> showError("Пользователь с таким номером телефона не зарегистрирован")
                    else -> showError("Пользователь не найден")
                }
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("wrong-password", true) == true -> {
                // Неверный пароль
                showError("Неверный пароль")
                binding.etPassword.requestFocus()
                binding.etPassword.text?.clear()
            }
            exception?.message?.contains("invalid-email", true) == true -> {
                // Неверный формат email
                showError("Неверный формат email")
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("network", true) == true -> {
                // Нет интернета
                checkOfflineAccess()
            }
            exception?.message?.contains("too-many-requests", true) == true -> {
                // Слишком много попыток
                showError("Слишком много попыток входа. Попробуйте позже")
            }
            else -> {
                // Другие ошибки
                showError("Пользователь с таким номером телефона или email не зарегистрирован")
            }
        }
    }

    private fun checkOfflineAccess() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Проверяем валидность пользователя для оффлайн режима
            verifyUserForOffline(currentUser)
        } else {
            showError("Нет подключения к интернету. Вход невозможен")
        }
    }

    private fun verifyUserForOffline(user: com.google.firebase.auth.FirebaseUser) {
        user.getIdToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showSuccess("Оффлайн режим. Добро пожаловать!")
                navigateToMain()
            } else {
                // Пользователь невалиден даже для оффлайн режима
                auth.signOut()
                showError("Сессия истекла. Требуется подключение к интернету")
            }
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
        finishAffinity()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    enum class InputType {
        EMAIL, PHONE, UNKNOWN
    }
}