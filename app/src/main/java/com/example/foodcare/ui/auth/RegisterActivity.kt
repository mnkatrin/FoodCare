package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.auth.UserManager // Убедитесь, что импортирован
import com.example.foodcare.databinding.ActivityRegisterBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint // <-- Добавлен импорт
import javax.inject.Inject // <-- Добавлен импорт

// <-- Добавлена аннотация
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    // --- ИНЖЕКТИРУЕМ UserManager ---
    @Inject lateinit var userManager: UserManager
    // --- КОНЕЦ ИНЖЕКТИРОВАНИЯ ---

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.button.setOnClickListener {
            performRegistration()
        }

        binding.imageButton3.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val email = binding.etEmail2.text.toString().trim()
        val password = binding.etPassword2.text.toString().trim()
        val name = binding.etName.text.toString().trim()

        // Валидация полей
        if (email.isEmpty()) {
            showError("Введите email")
            binding.etEmail2.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            binding.etEmail2.requestFocus()
            return
        }

        if (password.isEmpty()) {
            showError("Введите пароль")
            binding.etPassword2.requestFocus()
            return
        }

        if (name.isEmpty()) {
            showError("Введите имя")
            binding.etName.requestFocus()
            return
        }

        // ТОЛЬКО проверка длины - без ограничений на символы
        if (name.length < 2) {
            showError("Имя должно содержать минимум 2 символа")
            binding.etName.requestFocus()
            return
        }

        if (name.length > 50) {
            showError("Имя должно содержать не более 50 символов")
            binding.etName.requestFocus()
            return
        }

        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            binding.etPassword2.requestFocus()
            return
        }

        // Блокируем кнопку на время регистрации
        binding.button.isEnabled = false
        binding.button.text = "Регистрация..."

        registerWithEmail(email, password, name)
    }

    private fun registerWithEmail(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        updateUserProfile(it, name, email)
                    }
                } else {
                    binding.button.isEnabled = true
                    binding.button.text = "Зарегистрироваться"
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun updateUserProfile(
        user: com.google.firebase.auth.FirebaseUser,
        name: String,
        email: String
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                createUserInFirestore(user, name, email)
            }
            .addOnFailureListener {
                createUserInFirestore(user, name, email)
            }
    }

    private fun createUserInFirestore(
        user: com.google.firebase.auth.FirebaseUser,
        name: String,
        email: String
    ) {
        val userData = hashMapOf(
            "email" to email,
            "name" to name,
            "displayName" to name,
            "role" to "user",
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLogin" to FieldValue.serverTimestamp(),
            "isActive" to true,
            "preferences" to hashMapOf(
                "notifications" to true,
                "theme" to "light"
            )
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "=== РЕГИСТРАЦИЯ УСПЕШНА ===")

                // --- ИСПРАВЛЕНО: Сохраняем состояние входа через инжектированный UserManager ---
                // FoodCareApplication.saveLoginState(true, email) // <-- УБРАНО
                userManager.setUserEmail(email) // <-- Сохраняем email через UserManager
                // userManager.setUserName(name) // <-- При необходимости, сохраните имя тоже
                // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

                // --- ИСПРАВЛЕНО: Проверяем состояние через инжектированный UserManager ---
                // val (testIsLoggedIn, testEmail) = FoodCareApplication.getLoginState() // <-- УБРАНО
                val testIsLoggedIn = userManager.getLoginState() // <-- Получаем isLoggedIn через UserManager
                val testEmail = userManager.getCurrentUserEmail() // <-- Получаем email через UserManager
                // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

                Log.d(TAG, "ПРОВЕРКА СОХРАНЕНИЯ: isLoggedIn=$testIsLoggedIn, email=$testEmail")

                if (testIsLoggedIn && testEmail == email) {
                    Log.d(TAG, "СОХРАНЕНИЕ УСПЕШНО")
                    showSuccess("Регистрация успешна!")
                    navigateToMain()
                } else {
                    Log.e(TAG, "ОШИБКА СОХРАНЕНИЯ")
                    showError("Ошибка сохранения сессии.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка Firestore: ${e.message}")

                // ВСЕ РАВНО СОХРАНЯЕМ СОСТОЯНИЕ ВХОДА ДАЖЕ ЕСЛИ FIRESTORE НЕ СРАБОТАЛ
                // FoodCareApplication.saveLoginState(true, email) // <-- УБРАНО
                userManager.setUserEmail(email) // <-- Сохраняем email через UserManager
                showSuccess("Регистрация успешна!")
                navigateToMain()
            }
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("network", true) == true ->
                "Проверьте подключение к интернету"
            exception?.message?.contains("email-already-in-use", true) == true ->
                "Пользователь с таким email уже зарегистрирован"
            exception?.message?.contains("invalid-email", true) == true ->
                "Неверный формат email"
            exception?.message?.contains("weak-password", true) == true ->
                "Пароль слишком слабый. Используйте минимум 6 символов"
            exception?.message?.contains("operation-not-allowed", true) == true ->
                "Регистрация отключена. Обратитесь к администратору"
            else -> "Ошибка регистрации: ${exception?.message ?: "Неизвестная ошибка"}"
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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}