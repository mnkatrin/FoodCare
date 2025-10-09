package com.example.foodcare.ui.profile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.foodcare.R
import com.example.foodcare.databinding.ActivityProfileDrawerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileManager(private val context: Context, private val parentView: View) {

    private lateinit var binding: ActivityProfileDrawerBinding
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "ProfileManager"
    }

    // Интерфейс для обработки событий профиля
    interface ProfileListener {
        fun onLogoutRequested()
        fun onProfileHidden()
        fun onUserNameUpdated(newName: String)
    }

    private var profileListener: ProfileListener? = null

    fun setProfileListener(listener: ProfileListener) {
        this.profileListener = listener
    }

    fun initializeProfile(): ActivityProfileDrawerBinding {
        binding = ActivityProfileDrawerBinding.inflate(LayoutInflater.from(context))
        setupProfileData()
        setupProfileClickListeners()
        return binding
    }

    private fun setupProfileData() {
        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Устанавливаем данные пользователя
                val userName = currentUser.displayName ?: "Пользователь"
                binding.userNameEditText.setText(userName)

                val userContact = currentUser.email ?: currentUser.phoneNumber ?: "Контакт не указан"
                binding.userEmailText.text = userContact

                Log.d(TAG, "Profile data setup: $userName, $userContact")
            } else {
                // Если пользователь не найден, устанавливаем значения по умолчанию
                binding.userNameEditText.setText("Пользователь")
                binding.userEmailText.text = "email@example.com"
                Log.w(TAG, "No user found, using default values")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile data: ${e.message}")
            // Устанавливаем значения по умолчанию в случае ошибки
            binding.userNameEditText.setText("Пользователь")
            binding.userEmailText.text = "email@example.com"
        }
    }

    private fun setupProfileClickListeners() {
        try {
            // Редактирование имени по нажатию на карточку
            binding.userNameCard.setOnClickListener {
                enableNameEditing()
            }

            // Обработка завершения редактирования
            binding.userNameEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveUserName()
                    true
                } else {
                    false
                }
            }

            // Потеря фокуса - сохраняем изменения
            binding.userNameEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveUserName()
                }
            }

            // История
            binding.historyButton.setOnClickListener {
                hideProfile()
                Toast.makeText(context, "История в разработке", Toast.LENGTH_SHORT).show()
            }

            // Аллергены
            binding.allergensButton.setOnClickListener {
                hideProfile()
                Toast.makeText(context, "Аллергены в разработке", Toast.LENGTH_SHORT).show()
            }

            // Тема
            binding.themeButton.setOnClickListener {
                hideProfile()
                Toast.makeText(context, "Тема приложения в разработке", Toast.LENGTH_SHORT).show()
            }

            // Выход
            binding.logoutButton.setOnClickListener {
                hideProfile()
                showLogoutConfirmation()
            }

            Log.d(TAG, "Profile click listeners setup completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile click listeners: ${e.message}")
        }
    }

    private fun enableNameEditing() {
        try {
            binding.userNameEditText.apply {
                // Включаем возможность редактирования
                isFocusable = true
                isFocusableInTouchMode = true
                setCursorVisible(true)
                requestFocus()

                // Показываем клавиатуру
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

                // Перемещаем курсор в конец текста
                setSelection(text.length)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error enabling name editing: ${e.message}")
        }
    }

    private fun saveUserName() {
        try {
            val newName = binding.userNameEditText.text.toString().trim()

            if (newName.isEmpty()) {
                // Если имя пустое, восстанавливаем предыдущее
                setupProfileData()
                resetNameEditing()
                return
            }

            // Скрываем клавиатуру
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.userNameEditText.windowToken, 0)

            // Отключаем редактирование
            resetNameEditing()

            // Обновляем имя в Firebase
            updateUserName(newName)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving user name: ${e.message}")
            resetNameEditing()
            setupProfileData() // Восстанавливаем предыдущее имя
        }
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
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Успешно обновили имя в Firebase Auth
                        Log.d(TAG, "User name updated successfully: $newName")

                        // Уведомляем MainActivity об обновлении
                        profileListener?.onUserNameUpdated(newName)

                        // Без уведомления об успешном обновлении
                    } else {
                        // Ошибка при обновлении
                        Log.e(TAG, "Error updating user name: ${task.exception?.message}")

                        // Восстанавливаем предыдущее имя
                        setupProfileData()
                    }
                }
        } else {
            setupProfileData() // Восстанавливаем данные
        }
    }

    private fun showLogoutConfirmation() {
        Log.d(TAG, "Showing logout dialog")

        AlertDialog.Builder(context)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Выйти") { dialog, _ ->
                Log.d(TAG, "User confirmed logout")
                profileListener?.onLogoutRequested()
                dialog.dismiss()
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

    fun showProfile() {
        Log.d(TAG, "Showing profile")

        try {
            // Удаляем профиль если он уже добавлен (для пересоздания)
            if (binding.root.parent != null) {
                (binding.root.parent as? ViewGroup)?.removeView(binding.root)
            }

            // Ищем ConstraintLayout как родительский контейнер
            val parentContainer = parentView.findViewById<ConstraintLayout>(com.example.foodcare.R.id.main)

            // Создаем LayoutParams для правильного позиционирования
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )

            // Устанавливаем выравнивание слева и растягиваем по высоте
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

            binding.root.layoutParams = layoutParams

            // Устанавливаем ширину профиля
            binding.root.minimumWidth = (context.resources.displayMetrics.widthPixels * 0.8).toInt()

            parentContainer?.addView(binding.root)

            // Поднимаем профиль на самый верх
            binding.root.bringToFront()
            binding.root.visibility = View.VISIBLE

            // Принудительно запрашиваем перерисовку
            binding.root.post {
                binding.root.requestLayout()
                binding.root.invalidate()
            }

            Log.d(TAG, "Profile shown successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing profile: ${e.message}")
        }
    }

    fun hideProfile() {
        Log.d(TAG, "Hiding profile")

        try {
            // Если редактирование активно - сохраняем изменения перед скрытием
            if (binding.userNameEditText.isFocusable) {
                saveUserName()
            }

            binding.root.visibility = View.GONE
            profileListener?.onProfileHidden()
            Log.d(TAG, "Profile hidden successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding profile: ${e.message}")
        }
    }

    fun isProfileVisible(): Boolean {
        return binding.root.visibility == View.VISIBLE
    }

    fun updateUserData() {
        setupProfileData()
    }

    fun getProfileBinding(): ActivityProfileDrawerBinding {
        return binding
    }

    fun cleanup() {
        try {
            // Удаляем профиль из родительского view при очистке
            if (binding.root.parent != null) {
                (binding.root.parent as? ViewGroup)?.removeView(binding.root)
            }
            profileListener = null
            Log.d(TAG, "Profile manager cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up profile manager: ${e.message}")
        }
    }
}