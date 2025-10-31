package com.example.foodcare.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityProfileDrawerBinding
import com.example.foodcare.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileClass : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDrawerBinding
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserData()
        setupClickListeners()
    }

    private fun setupUserData() {
        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Устанавливаем данные пользователя из Firebase
                val userName = currentUser.displayName ?: "Пользователь"
                binding.userNameEditText.setText(userName)

                val userContact = currentUser.email ?: currentUser.phoneNumber ?: "Контакт не указан"
                binding.userEmailText.text = userContact
            } else {
                // Если пользователь не найден, устанавливаем значения по умолчанию
                binding.userNameEditText.setText("Пользователь")
                binding.userEmailText.text = "email@example.com"
            }

        } catch (e: Exception) {
            // Устанавливаем значения по умолчанию в случае ошибки
            binding.userNameEditText.setText("Пользователь")
            binding.userEmailText.text = "email@example.com"
        }
    }

    private fun setupClickListeners() {
        // Кнопка истории
        binding.historyButton.setOnClickListener {
            Toast.makeText(this, "История в разработке", Toast.LENGTH_SHORT).show()
        }

        // Кнопка аллергенов
        binding.allergensButton.setOnClickListener {
            Toast.makeText(this, "Аллергены в разработке", Toast.LENGTH_SHORT).show()
        }

        // Кнопка темы приложения
        binding.themeButton.setOnClickListener {
            Toast.makeText(this, "Тема приложения в разработке", Toast.LENGTH_SHORT).show()
        }

        // Кнопка выхода
        binding.logoutButton.setOnClickListener {
            performLogout()
        }

        // Карточка имени пользователя для редактирования
        binding.userNameCard.setOnClickListener {
            // Включить редактирование имени
            enableNameEditing()
        }

        // Обработка завершения редактирования имени
        binding.userNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                saveUserName()
                true
            } else {
                false
            }
        }

        // Сохранение имени при потере фокуса
        binding.userNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveUserName()
            }
        }
    }

    private fun enableNameEditing() {
        binding.userNameEditText.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setCursorVisible(true)
            requestFocus()

            // Показываем клавиатуру
            val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

            // Перемещаем курсор в конец текста
            setSelection(text.length)
        }
    }

    private fun saveUserName() {
        val newName = binding.userNameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            // Если имя пустое, восстанавливаем предыдущее
            setupUserData()
            resetNameEditing()
            return
        }

        // Скрываем клавиатуру
        val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.userNameEditText.windowToken, 0)

        // Отключаем редактирование
        resetNameEditing()

        // Обновляем имя в Firebase
        updateUserName(newName)
    }

    private fun resetNameEditing() {
        binding.userNameEditText.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setCursorVisible(false)
            clearFocus()
        }
    }

    private fun updateUserName(newName: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Создаем запрос на обновление профиля
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Успешно обновили имя
                        Toast.makeText(this, "Имя обновлено", Toast.LENGTH_SHORT).show()
                    } else {
                        // Ошибка при обновлении
                        Toast.makeText(this, "Ошибка обновления имени", Toast.LENGTH_SHORT).show()
                        // Восстанавливаем предыдущее имя
                        setupUserData()
                    }
                }
        } else {
            setupUserData() // Восстанавливаем данные
        }
    }

    private fun performLogout() {
        try {
            auth.signOut()
            Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при выходе", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Возврат к предыдущему экрану
    }
}