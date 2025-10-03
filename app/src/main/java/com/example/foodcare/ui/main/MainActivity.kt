package com.example.foodcare.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.foodcare.databinding.ActivityMainBinding
import com.example.foodcare.ui.auth.LoginActivity
import com.example.foodcare.ui.base.FullScreenActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : FullScreenActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val auth = Firebase.auth

    // Переменные для перетаскивания
    private var xDelta = 0f
    private var yDelta = 0f
    private var isDragging = false
    private val CLICK_THRESHOLD = 10f

    companion object {
        private const val TAG = "MainActivity"
        private const val PREF_WELCOME_SHOWN = "welcome_shown"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        Log.d(TAG, "MainActivity created")

        // Проверяем, нужно ли показать приветствие
        checkAndShowWelcome()

        // Ждем когда layout будет полностью отрисован
        binding.root.post {
            Log.d(TAG, "Setting up draggable button")
            setupDraggableButton()
        }

        setupClickListeners()
    }

    private fun checkAndShowWelcome() {
        try {
            // Проверяем флаг из интента (после авторизации)
            val showWelcome = intent.getBooleanExtra("SHOW_WELCOME", false)

            if (showWelcome) {
                showWelcomeMessage()
                // Помечаем, что приветствие уже показано
                sharedPreferences.edit().putBoolean(PREF_WELCOME_SHOWN, true).apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking welcome: ${e.message}")
        }
    }

    private fun showWelcomeMessage() {
        try {
            val currentUser = auth.currentUser
            val userName = currentUser?.displayName ?: currentUser?.email ?: "Пользователь"

            val welcomeMessage = "$userName, добро пожаловать!"

            Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show()
            Log.d(TAG, "Welcome message shown: $welcomeMessage")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing welcome message: ${e.message}")
            Toast.makeText(this, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")

        try {
            // Обработчики для других кнопок
            binding.btnRecipes.setOnClickListener {
                Log.d(TAG, "Recipes button clicked")
                Toast.makeText(this, "Раздел 'Рецепты' в разработке", Toast.LENGTH_SHORT).show()
            }

            binding.btnProducts.setOnClickListener {
                Log.d(TAG, "Products button clicked")
                Toast.makeText(this, "Раздел 'Продукты' в разработке", Toast.LENGTH_SHORT).show()
            }

            Log.d(TAG, "All click listeners set up successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting click listeners: ${e.message}")
            Toast.makeText(this, "Ошибка настройки кнопок", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDraggableButton() {
        Log.d(TAG, "Setting up draggable button")

        try {
            val draggableButton = binding.profileButton
            val parentFrame = binding.bottomFrame

            // Загружаем сохраненное положение кнопки профиля
            val savedX = sharedPreferences.getFloat("profile_button_x", -1f)
            val savedY = sharedPreferences.getFloat("profile_button_y", -1f)

            Log.d(TAG, "Saved position: x=$savedX, y=$savedY")

            if (savedX != -1f && savedY != -1f) {
                draggableButton.x = savedX
                draggableButton.y = savedY
                Log.d(TAG, "Restored saved position")
            } else {
                setInitialButtonPosition()
                Log.d(TAG, "Set initial position")
            }

            draggableButton.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        xDelta = view.x - event.rawX
                        yDelta = view.y - event.rawY
                        isDragging = false
                        view.alpha = 0.7f
                        Log.d(TAG, "Touch down on profile button")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val moveX = event.rawX + xDelta
                        val moveY = event.rawY + yDelta

                        // Проверяем, было ли движение достаточным для перетаскивания
                        if (Math.abs(moveX - view.x) > CLICK_THRESHOLD ||
                            Math.abs(moveY - view.y) > CLICK_THRESHOLD) {
                            isDragging = true
                        }

                        val parent = view.parent as android.view.View
                        val maxX = parent.width - view.width
                        val maxY = parent.height - view.height

                        val newX = moveX.coerceIn(0f, maxX.toFloat())
                        val newY = moveY.coerceIn(0f, maxY.toFloat())

                        view.x = newX
                        view.y = newY

                        Log.d(TAG, "Button moved to: x=$newX, y=$newY")
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1.0f

                        if (!isDragging) {
                            // Если не было перетаскивания - это клик
                            Log.d(TAG, "Profile button CLICKED!")
                            showLogoutConfirmation()
                        }

                        saveButtonPosition(view.x, view.y)
                        Log.d(TAG, "Touch ended, position saved, isDragging=$isDragging")
                    }
                }
                true
            }

            Log.d(TAG, "Draggable button setup completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up draggable button: ${e.message}")
        }
    }

    private fun showLogoutConfirmation() {
        Log.d(TAG, "Showing logout dialog")

        AlertDialog.Builder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Выйти") { dialog, _ ->
                Log.d(TAG, "User confirmed logout")
                performLogout()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                Log.d(TAG, "User canceled logout")
                dialog.dismiss()
            }
            .setOnCancelListener {
                Log.d(TAG, "Logout dialog canceled")
            }
            .show()
    }

    private fun performLogout() {
        Log.d(TAG, "Performing logout")

        try {
            // Выход из Firebase Auth
            auth.signOut()
            Log.d(TAG, "Firebase auth signed out")

            // Очищаем все локальные данные
            clearAllLocalData()

            // Показываем сообщение
            Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()

            // Переходим на экран авторизации
            navigateToLogin()

        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}")
            Toast.makeText(this, "Ошибка при выходе: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllLocalData() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "SharedPreferences cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data: ${e.message}")
        }
    }

    private fun navigateToLogin() {
        Log.d(TAG, "Navigating to LoginActivity")

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setInitialButtonPosition() {
        try {
            val draggableButton = binding.profileButton
            val parentFrame = binding.bottomFrame

            parentFrame.post {
                draggableButton.x = parentFrame.width - draggableButton.width - 20f
                draggableButton.y = parentFrame.height - draggableButton.height - 20f

                saveButtonPosition(draggableButton.x, draggableButton.y)
                Log.d(TAG, "Initial position set")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting initial position: ${e.message}")
        }
    }

    private fun saveButtonPosition(x: Float, y: Float) {
        try {
            sharedPreferences.edit()
                .putFloat("profile_button_x", x)
                .putFloat("profile_button_y", y)
                .apply()
            Log.d(TAG, "Position saved: x=$x, y=$y")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving position: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        saveButtonPosition(binding.profileButton.x, binding.profileButton.y)
    }
}