package com.example.foodcare.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.foodcare.R
import com.example.foodcare.databinding.ActivityMainBinding
import com.example.foodcare.ui.auth.LoginActivity
import com.example.foodcare.ui.base.FullScreenActivity
import com.example.foodcare.ui.app_product.AddProductFragment
import com.example.foodcare.ui.products.ProductsFragment
import com.example.foodcare.ui.profile.ProfileManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : FullScreenActivity(), ProfileManager.ProfileListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileManager: ProfileManager
    private lateinit var sharedPreferences: SharedPreferences
    private val auth = Firebase.auth

    // Переменные для перетаскивания
    private var xDelta = 0f
    private var yDelta = 0f
    private var isDragging = false
    private val clickThreshold = 10f

    companion object {
        private const val TAG = "MainActivity"
        private const val PREF_WELCOME_SHOWN = "welcome_shown"
        private const val PROFILE_BUTTON_X = "profile_button_x"
        private const val PROFILE_BUTTON_Y = "profile_button_y"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем менеджер профиля
        profileManager = ProfileManager(this, binding.root)
        profileManager.setProfileListener(this)
        profileManager.initializeProfile()

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Проверяем, нужно ли показать приветствие
        checkAndShowWelcome()

        // Ждем когда layout будет полностью отрисован
        binding.root.post {
            setupDraggableButton()
        }

        setupClickListeners()
    }

    override fun onUserNameUpdated(newName: String) {
        // Обработчик обновления имени
    }

    override fun onLogoutRequested() {
        performLogout()
    }

    override fun onProfileHidden() {
        binding.backgroundDim.visibility = View.GONE
    }

    private fun showProfile() {
        profileManager.showProfile()
        binding.backgroundDim.visibility = View.VISIBLE
    }

    private fun hideProfile() {
        profileManager.hideProfile()
    }

    private fun checkAndShowWelcome() {
        try {
            val showWelcome = intent.getBooleanExtra("SHOW_WELCOME", false)
            if (showWelcome) {
                showWelcomeMessage()
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
        } catch (e: Exception) {
            Toast.makeText(this, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        try {
            // Кнопка рецептов
            binding.btnRecipes.setOnClickListener {
                Toast.makeText(this, "Раздел 'Рецепты' в разработке", Toast.LENGTH_SHORT).show()
            }

            // Кнопка продуктов - открываем список продуктов
            binding.btnProducts.setOnClickListener {
                openProductsFragment()
            }

            // Центральная круглая кнопка
            binding.imageButton4.setOnClickListener {
                // Действие для центральной кнопки
            }

            // Кнопка Button4 - открываем добавление продуктов
            binding.Button4.setOnClickListener {
                openAddProductFragment()
            }

            // Другие кнопки в нижней панели
            binding.Button3.setOnClickListener {
                // Другое действие для кнопки 3
            }

            binding.Button5.setOnClickListener {
                // Другое действие для кнопки 5
            }

            // Клик на затемнение для закрытия профиля
            binding.backgroundDim.setOnClickListener {
                hideProfile()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка настройки кнопок", Toast.LENGTH_LONG).show()
        }
    }

    private fun openAddProductFragment() {
        try {
            // Скрываем основной контент и показываем контейнер для фрагментов
            hideMainContent()
            binding.fragmentContainer.visibility = View.VISIBLE

            val addProductFragment = com.example.foodcare.ui.app_product.AddProductFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addProductFragment)
                .addToBackStack("addProduct")
                .commit()

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка открытия добавления продуктов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openProductsFragment() {
        try {
            // Скрываем основной контент и показываем контейнер для фрагментов
            hideMainContent()
            binding.fragmentContainer.visibility = View.VISIBLE

            val productsFragment = ProductsFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, productsFragment)
                .addToBackStack("products")
                .commit()

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка открытия раздела продуктов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideMainContent() {
        // Скрываем все основные элементы главного экрана
        binding.foodCareLayout.visibility = View.GONE
        binding.textView6.visibility = View.GONE
        binding.buttonsContainer.visibility = View.GONE
        binding.imageView5.visibility = View.GONE
        binding.textView5.visibility = View.GONE
        binding.imageView6.visibility = View.GONE
        binding.textView7.visibility = View.GONE
        binding.imageView7.visibility = View.GONE
        binding.imageView8.visibility = View.GONE
        binding.textView8.visibility = View.GONE
        binding.imageView13.visibility = View.GONE
        binding.textView11.visibility = View.GONE
        binding.imageView15.visibility = View.GONE
        binding.imageView14.visibility = View.GONE
        binding.textView12.visibility = View.GONE
        binding.imageView9.visibility = View.GONE
        binding.textView9.visibility = View.GONE
        binding.imageView10.visibility = View.GONE
        binding.imageView11.visibility = View.GONE
        binding.textView10.visibility = View.GONE
    }

    private fun showMainContent() {
        // Показываем все основные элементы главного экрана
        binding.foodCareLayout.visibility = View.VISIBLE
        binding.textView6.visibility = View.VISIBLE
        binding.buttonsContainer.visibility = View.VISIBLE
        binding.imageView5.visibility = View.VISIBLE
        binding.textView5.visibility = View.VISIBLE
        binding.imageView6.visibility = View.VISIBLE
        binding.textView7.visibility = View.VISIBLE
        binding.imageView7.visibility = View.VISIBLE
        binding.imageView8.visibility = View.VISIBLE
        binding.textView8.visibility = View.VISIBLE
        binding.imageView13.visibility = View.VISIBLE
        binding.textView11.visibility = View.VISIBLE
        binding.imageView15.visibility = View.VISIBLE
        binding.imageView14.visibility = View.VISIBLE
        binding.textView12.visibility = View.VISIBLE
        binding.imageView9.visibility = View.VISIBLE
        binding.textView9.visibility = View.VISIBLE
        binding.imageView10.visibility = View.VISIBLE
        binding.imageView11.visibility = View.VISIBLE
        binding.textView10.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        // Если открыт фрагмент - закрываем его и показываем главный экран
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            binding.fragmentContainer.visibility = View.GONE
            showMainContent()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupDraggableButton() {
        try {
            val draggableButton = binding.profileButton

            // Загружаем сохраненное положение кнопки профиля
            val savedX = sharedPreferences.getFloat(PROFILE_BUTTON_X, -1f)
            val savedY = sharedPreferences.getFloat(PROFILE_BUTTON_Y, -1f)

            if (savedX != -1f && savedY != -1f) {
                draggableButton.x = savedX
                draggableButton.y = savedY
            } else {
                setInitialButtonPosition()
            }

            draggableButton.setOnTouchListener { view, event ->
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        xDelta = view.x - event.rawX
                        yDelta = view.y - event.rawY
                        isDragging = false
                        view.alpha = 0.7f
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val moveX = event.rawX + xDelta
                        val moveY = event.rawY + yDelta

                        if (Math.abs(moveX - view.x) > clickThreshold ||
                            Math.abs(moveY - view.y) > clickThreshold) {
                            isDragging = true
                        }

                        val parent = view.parent as? android.view.View ?: return@setOnTouchListener true
                        val maxX = parent.width - view.width
                        val maxY = parent.height - view.height

                        val newX = moveX.coerceIn(0f, maxX.toFloat())
                        val newY = moveY.coerceIn(0f, maxY.toFloat())

                        view.x = newX
                        view.y = newY
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1.0f

                        if (!isDragging) {
                            showProfile()
                        }

                        saveButtonPosition(view.x, view.y)
                    }
                }
                true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up draggable button: ${e.message}")
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Выйти") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        try {
            auth.signOut()
            clearAllLocalData()
            Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при выходе", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllLocalData() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data: ${e.message}")
        }
    }

    private fun navigateToLogin() {
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
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting initial position: ${e.message}")
        }
    }

    private fun saveButtonPosition(x: Float, y: Float) {
        try {
            sharedPreferences.edit()
                .putFloat(PROFILE_BUTTON_X, x)
                .putFloat(PROFILE_BUTTON_Y, y)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving position: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            saveButtonPosition(binding.profileButton.x, binding.profileButton.y)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        profileManager.cleanup()
    }
}