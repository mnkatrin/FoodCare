package com.example.foodcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityRegisterBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

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
        val emailOrPhone = binding.etEmail2.text.toString().trim()
        val password = binding.etPassword2.text.toString().trim()
        val name = binding.etName.text.toString().trim()

        // Валидация полей
        if (emailOrPhone.isEmpty()) {
            showError("Введите email или номер телефона")
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


        // Определяем тип ввода (email или телефон)
        val inputType = determineInputType(emailOrPhone)
        if (inputType == InputType.UNKNOWN) {
            showError("Введите корректный email или номер телефона")
            binding.etEmail2.requestFocus()
            return
        }

        // Блокируем кнопку на время регистрации
        binding.button.isEnabled = false
        binding.button.text = "Регистрация..."

        // Регистрация в зависимости от типа ввода
        when (inputType) {
            InputType.EMAIL -> registerWithEmail(emailOrPhone, password, name)
            InputType.PHONE -> registerWithPhone(emailOrPhone, password, name)
            else -> {
                binding.button.isEnabled = true
                binding.button.text = "Зарегистрироваться"
            }
        }
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

    private fun registerWithEmail(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        updateUserProfile(it, name, email, "")
                    }
                } else {
                    binding.button.isEnabled = true
                    binding.button.text = "Зарегистрироваться"
                    handleRegistrationError(task.exception, InputType.EMAIL)
                }
            }
    }

    private fun registerWithPhone(phone: String, password: String, name: String) {
        val formattedPhone = formatPhoneNumber(phone)
        val tempEmail = "${formattedPhone.replace("[^0-9]".toRegex(), "")}@foodcare.com"

        auth.createUserWithEmailAndPassword(tempEmail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        updateUserProfile(it, name, tempEmail, formattedPhone)
                    }
                } else {
                    binding.button.isEnabled = true
                    binding.button.text = "Зарегистрироваться"
                    handleRegistrationError(task.exception, InputType.PHONE)
                }
            }
    }

    private fun updateUserProfile(
        user: com.google.firebase.auth.FirebaseUser,
        name: String,
        email: String,
        phone: String
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name) // Поддерживает любые символы включая русские
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                createUserInFirestore(user, name, email, phone)
            }
            .addOnFailureListener {
                createUserInFirestore(user, name, email, phone)
            }
    }

    private fun createUserInFirestore(
        user: com.google.firebase.auth.FirebaseUser,
        name: String,
        email: String,
        phone: String
    ) {
        val originalInput = binding.etEmail2.text.toString().trim()

        val userData = hashMapOf(
            "email" to email,
            "name" to name, // Сохраняем имя как есть (русские символы)
            "displayName" to name,
            "phone" to phone,
            "originalLogin" to originalInput,
            "loginType" to if (phone.isNotEmpty()) "phone" else "email",
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
                showSuccess("Регистрация успешна!")
                navigateToMain()
            }
            .addOnFailureListener { e ->
                showSuccess("Регистрация успешна!")
                navigateToMain()
            }
    }

    private fun handleRegistrationError(exception: Exception?, inputType: InputType) {
        val errorMessage = when {
            exception?.message?.contains("network", true) == true ->
                "Проверьте подключение к интернету"
            exception?.message?.contains("email-already-in-use", true) == true ->
                if (inputType == InputType.EMAIL) {
                    "Пользователь с таким email уже зарегистрирован"
                } else {
                    "Пользователь с таким номером телефона уже зарегистрирован"
                }
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    enum class InputType {
        EMAIL, PHONE, UNKNOWN
    }
}