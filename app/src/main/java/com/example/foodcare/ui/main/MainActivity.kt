package com.example.foodcare.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import com.example.foodcare.FoodCareApplication // Убедитесь, что импортирован
import com.example.foodcare.auth.UserManager // Убедитесь, что импортирован
import com.example.foodcare.R
import com.example.foodcare.databinding.ActivityMainBinding
import com.example.foodcare.ui.auth.LoginActivity
import com.example.foodcare.ui.base.FullScreenActivity
import com.example.foodcare.ui.app_product.AddProductFragment
import com.example.foodcare.ui.products.ProductsFragment
import com.example.foodcare.ui.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

// --- УБРАНО: @AndroidEntryPoint ---
// import dagger.hilt.android.AndroidEntryPoint

// Убираем аннотацию
class MainActivity : FullScreenActivity() { // Убираем @AndroidEntryPoint

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout

    // --- ИЗМЕНЕНО: Получаем UserManager вручную ---
    private val userManager: UserManager by lazy {
        val application = application as FoodCareApplication
        application.userManager
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // Переменные для перетаскивания
    private var xDelta = 0f
    private var yDelta = 0f
    private var isDragging = false
    private val clickThreshold = 10f

    companion object {
        private const val PREF_FIRST_LAUNCH = "first_launch"
        private const val PROFILE_BUTTON_X = "profile_button_x"
        private const val PROFILE_BUTTON_Y = "profile_button_y"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeApp()
        setupDrawerLayout()
    }

    private fun setupDrawerLayout() {
        drawerLayout = binding.drawerLayout

        val profileFragment = ProfileFragment()
        profileFragment.setProfileListener(object : ProfileFragment.ProfileListener {
            override fun onLogoutRequested() {
                drawerLayout.closeDrawers()
                showLogoutConfirmation()
            }
        })

        supportFragmentManager.beginTransaction()
            .replace(R.id.profile_container, profileFragment)
            .commit()
    }

    private fun initializeApp() {
        // Проверяем первый запуск
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        checkFirstLaunch()

        // Настройка кнопок
        binding.root.post {
            setupDraggableButton()
        }

        setupClickListeners()
    }

    private fun checkFirstLaunch() {
        val isFirstLaunch = sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            onFirstLaunch()
            sharedPreferences.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply()
        }
    }

    private fun onFirstLaunch() {
        setInitialButtonPosition()
    }

    private fun setupClickListeners() {
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

        // Кнопка профиля - открываем Drawer слева
        binding.profileButton.setOnClickListener {
            openProfile()
        }

        // Другие кнопки в нижней панели
        binding.Button3.setOnClickListener {
            // Другое действие для кнопки 3
        }

        binding.Button5.setOnClickListener {
            // Другое действие для кнопки 5
        }
    }

    private fun openProfile() {
        // Открываем Drawer с профилем слева
        drawerLayout.openDrawer(binding.profileContainer)
    }

    private fun openAddProductFragment() {
        hideMainContent()
        binding.fragmentContainer.visibility = View.VISIBLE

        val addProductFragment = AddProductFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addProductFragment)
            .addToBackStack("addProduct")
            .commit()
    }

    private fun openProductsFragment() {
        hideMainContent()
        binding.fragmentContainer.visibility = View.VISIBLE

        val productsFragment = ProductsFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, productsFragment)
            .addToBackStack("products")
            .commit()
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
        if (drawerLayout.isDrawerOpen(binding.profileContainer)) {
            // Если открыт профиль - закрываем его
            drawerLayout.closeDrawers()
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            binding.fragmentContainer.visibility = View.GONE
            showMainContent()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupDraggableButton() {
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

                    val parent = view.parent as? View ?: return@setOnTouchListener true
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
                        openProfile() // Открываем профиль при клике
                    }

                    saveButtonPosition(view.x, view.y)
                }
            }
            true
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
        // --- ИСПРАВЛЕНО: Выполняем logout через инжектированный UserManager ---
        userManager.logout() // <-- Вызываем logout через UserManager
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        // Выход из Firebase (UserManager уже вызвал signOut, если нужно)
        // FirebaseAuth.getInstance().signOut() // <-- УБРАНО, т.к. UserManager уже делает это

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun saveButtonPosition(x: Float, y: Float) {
        sharedPreferences.edit().apply {
            putFloat(PROFILE_BUTTON_X, x)
            putFloat(PROFILE_BUTTON_Y, y)
            commit() // ИСПОЛЬЗУЕМ commit() ВМЕСТО apply()
        }
    }

    private fun setInitialButtonPosition() {
        val draggableButton = binding.profileButton
        val parentFrame = binding.bottomFrame

        parentFrame.post {
            draggableButton.x = parentFrame.width - draggableButton.width - 20f
            draggableButton.y = parentFrame.height - draggableButton.height - 20f
            saveButtonPosition(draggableButton.x, draggableButton.y)
        }
    }


    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            saveButtonPosition(binding.profileButton.x, binding.profileButton.y)
        }
    }
}