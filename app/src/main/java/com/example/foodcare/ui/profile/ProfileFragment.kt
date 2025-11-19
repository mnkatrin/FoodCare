package com.example.foodcare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodcare.R
import com.example.foodcare.history.HistoryFragment  // Импортируйте ваш фрагмент истории
import com.example.foodcare.databinding.ActivityProfileDrawerBinding
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
        // Обработчик для кнопки истории
        binding.historyButton.setOnClickListener {
            // Открываем фрагмент истории
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HistoryFragment())  // Замените на правильный контейнер
                .addToBackStack(null)  // Добавляем в стек возврата
                .commit()
        }

        binding.logoutButton.setOnClickListener {
            profileListener?.onLogoutRequested()
        }

        // Другие кнопки и действия
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
