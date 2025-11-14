package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.FoodCareApplication // Убедитесь, что импортирован
import com.example.foodcare.auth.UserManager // Убедитесь, что импортирован
import com.example.foodcare.databinding.ActivityLoginBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
// --- УБРАНО: import dagger.hilt.android.AndroidEntryPoint
// --- УБРАНО: import javax.inject.Inject

// --- УБРАНО: @AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    // --- ИЗМЕНЕНО: Получаем UserManager из Application ---
    private val userManager: UserManager by lazy {
        (application as FoodCareApplication).userManager
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        auth.setLanguageCode("ru")

        setupClickListeners()
        checkAutoLogin()
    }

    private fun debugCurrentState(location: String) {
        val isLoggedIn = userManager.getLoginState()
        val savedEmail = userManager.getCurrentUserEmail()

        Log.d(TAG, "=== DEBUG $location ===")
        Log.d(TAG, "App State - is_logged_in: $isLoggedIn")
        Log.d(TAG, "App State - user_email: $savedEmail")
        Log.d(TAG, "Firebase Auth - currentUser: ${auth.currentUser}")
        Log.d(TAG, "Firebase Auth - user UID: ${auth.currentUser?.uid}")
        Log.d(TAG, "Firebase Auth - user email: ${auth.currentUser?.email}")
    }

    private fun checkAutoLogin() {
        val isLoggedIn = userManager.getLoginState()
        val savedEmail = userManager.getCurrentUserEmail()
        val firebaseUser = auth.currentUser

        if (firebaseUser != null && isLoggedIn) {
            Log.d(TAG, "Автоматический вход для: $savedEmail")
            navigateToMain()
        } else if (firebaseUser != null) {
            Log.d(TAG, "Firebase user существует, синхронизируем состояние")
            userManager.setUserEmail(firebaseUser.email ?: "")
            navigateToMain()
        }
    }

    private fun attemptAutoLogin(savedEmail: String) {
        binding.etEmail.setText(savedEmail)
        binding.etPassword.requestFocus()
        Toast.makeText(this, "Введите пароль для $savedEmail", Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        binding.button2.setOnClickListener {
            performLogin()
        }

        binding.button3.setOnClickListener {
            navigateToRegister()
        }

        binding.resetPasswordButton.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

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

        binding.button2.isEnabled = false
        binding.button2.text = "Вход..."

        val inputType = determineInputType(email)
        if (inputType == InputType.UNKNOWN) {
            showError("Введите корректный email или номер телефона")
            binding.etEmail.requestFocus()
            binding.button2.isEnabled = true
            binding.button2.text = "Войти"
            return
        }

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
                    handleSuccessfulLogin(emailOrPhone)
                } else {
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

    private fun handleSuccessfulLogin(emailOrPhone: String) {
        Log.d(TAG, "=== УСПЕШНЫЙ ВХОД ===")

        userManager.setUserEmail(emailOrPhone)

        val testIsLoggedIn = userManager.getLoginState()
        val testEmail = userManager.getCurrentUserEmail()

        Log.d(TAG, "ПРОВЕРКА СОХРАНЕНИЯ: isLoggedIn=$testIsLoggedIn, email=$testEmail")

        if (testIsLoggedIn && testEmail == emailOrPhone) {
            Log.d(TAG, "СОХРАНЕНИЕ УСПЕШНО")
            showSuccess("Успешный вход!")
            navigateToMain()
        } else {
            Log.e(TAG, "ОШИБКА СОХРАНЕНИЯ Данные не сохранились")
            showError("Ошибка сохранения сессии. Попробуйте снова.")
        }
    }

    private fun handleLoginError(exception: Exception?, emailOrPhone: String, password: String, inputType: InputType) {
        when {
            exception is FirebaseAuthInvalidUserException -> {
                when (inputType) {
                    InputType.EMAIL -> showError("Пользователь с таким email не существует")
                    InputType.PHONE -> showError("Пользователь с таким номером телефона не существует")
                    else -> showError("Пользователь не существует")
                }
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("user-not-found", true) == true -> {
                when (inputType) {
                    InputType.EMAIL -> showError("Пользователь с таким email не зарегистрирован")
                    InputType.PHONE -> showError("Пользователь с таким номером телефона не зарегистрирован")
                    else -> showError("Пользователь не найден")
                }
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("wrong-password", true) == true -> {
                showError("Неверный пароль")
                binding.etPassword.requestFocus()
                binding.etPassword.text?.clear()
            }
            exception?.message?.contains("invalid-email", true) == true -> {
                showError("Неверный формат email")
                binding.etEmail.requestFocus()
            }
            exception?.message?.contains("network", true) == true -> {
                checkOfflineAccess()
            }
            exception?.message?.contains("too-many-requests", true) == true -> {
                showError("Слишком много попыток входа. Попробуйте позже")
            }
            else -> {
                showError("Пользователь с таким номером телефона или email не зарегистрирован")
            }
        }
    }

    private fun checkOfflineAccess() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
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
        Log.d(TAG, "Переход в MainActivity")

        debugCurrentState("navigateToMain")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_WELCOME", true)
        }
        startActivity(intent)
        finish()

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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