package com.example.foodcare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.foodcare.databinding.ActivityProfileDrawerBinding
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileDrawerBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    interface ProfileListener {
        fun onLogoutRequested()
    }

    private var profileListener: ProfileListener? = null

    fun setProfileListener(listener: ProfileListener) {
        this.profileListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserData()
        setupClickListeners()
    }

    private fun setupUserData() {
        try {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userName = currentUser.displayName ?: "Пользователь"
                binding.userNameEditText.setText(userName)

                val userContact = currentUser.email ?: currentUser.phoneNumber ?: "Контакт не указан"
                binding.userEmailText.text = userContact
            } else {
                binding.userNameEditText.setText("Пользователь")
                binding.userEmailText.text = "email@example.com"
            }

        } catch (e: Exception) {
            binding.userNameEditText.setText("Пользователь")
            binding.userEmailText.text = "email@example.com"
        }
    }

    private fun setupClickListeners() {
        binding.historyButton.setOnClickListener {
            showToast("История в разработке")
        }

        binding.allergensButton.setOnClickListener {
            showToast("Аллергены в разработке")
        }

        binding.themeButton.setOnClickListener {
            showToast("Тема приложения в разработке")
        }

        binding.logoutButton.setOnClickListener {
            profileListener?.onLogoutRequested()
        }

        binding.userNameCard.setOnClickListener {
            enableNameEditing()
        }

        binding.userNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                saveUserName()
                true
            } else {
                false
            }
        }

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

            val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            setSelection(text.length)
        }
    }

    private fun saveUserName() {
        val newName = binding.userNameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            setupUserData()
            resetNameEditing()
            return
        }

        val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.userNameEditText.windowToken, 0)

        resetNameEditing()
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
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast("Имя обновлено")
                    } else {
                        showToast("Ошибка обновления имени")
                        setupUserData()
                    }
                }
        } else {
            setupUserData()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}