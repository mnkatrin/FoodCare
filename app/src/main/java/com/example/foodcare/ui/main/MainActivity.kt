package com.example.foodcare.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Показываем приветствие
        showWelcomeMessage()

        setupClickListeners()
    }

    private fun showWelcomeMessage() {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: "Пользователь"
        Toast.makeText(this, "Добро пожаловать, $userEmail!", Toast.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        // Кнопка "Рецепты"
        binding.btnRecipes.setOnClickListener {
            Toast.makeText(this, "Раздел 'Рецепты' скоро будет доступен!", Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Продукты"
        binding.btnProducts.setOnClickListener {
            Toast.makeText(this, "Раздел 'Продукты' скоро будет доступен!", Toast.LENGTH_SHORT).show()
        }
    }
}