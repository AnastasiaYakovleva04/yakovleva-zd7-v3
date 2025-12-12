package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import android.util.Log

class SignInActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var name: EditText
    private lateinit var login: Button
    private lateinit var role: Spinner
    private val roles = arrayOf("Клиент", "Работник", "Поставщик")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        name = findViewById(R.id.name)
        login = findViewById(R.id.login)
        role = findViewById(R.id.role)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        role.adapter = adapter

        login.setOnClickListener {
            performRegistration()
        }
    }

    //вход-регистрация
    private fun performRegistration() {
        if (email.text.isNullOrEmpty() || password.text.isNullOrEmpty() || name.text.isNullOrEmpty()) {
            showSnackBar("Все поля должны быть заполнены")
            return
        }

        if (password.text.length < 8) {
            showSnackBar("Длина пароля должна быть минимум 8 символов")
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(this@SignInActivity)
                val userEmail = email.text.toString()
                val userPassword = password.text.toString()

                // попытка войти с существующими данными
                val existingUser = db.userDao().login(userEmail, userPassword)

                if (existingUser != null) {
                    runOnUiThread {
                        saveUserSession(existingUser)
                        navigateToMainActivity(existingUser)
                    }
                } else {
                    try {
                        val allUsers = db.userDao().getAllUsers()
                        val userByEmail = allUsers.find { it.email == userEmail }
                        if (userByEmail != null) {
                            runOnUiThread {
                                showSnackBar("Неверный пароль")
                            }
                        } else {
                            // пользователь не существует - регистрируем нового
                            registerNewUser(db, userEmail, userPassword)
                        }
                    } catch (e: Exception) {
                        registerNewUser(db, userEmail, userPassword)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showSnackBar("Ошибка: ${e.localizedMessage}")
                }
            }
        }.start()
    }

    //регистрация пользователя
    private fun registerNewUser(db: AppDatabase, email: String, password: String) {
        val selectedRole = role.selectedItem.toString()
        val newUser = User(
            name = name.text.toString(),
            email = email,
            password = password,
            role = selectedRole,
            discount = if (selectedRole == "Клиент") 5.0 else 0.0
        )

        val userId = db.userDao().insert(newUser)
        val userWithId = newUser.copy(id = userId.toInt())

        runOnUiThread {
            saveUserSession(userWithId)
            navigateToMainActivity(userWithId)
        }
    }

    //сохранение сессии пользователя
    private fun saveUserSession(user: User) {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_role", user.role)
            putInt("user_id", user.id)
            putBoolean("is_logged_in", true)
            apply()
        }
        Log.d("SignInActivity", "Сохранена сессия: userId=${user.id}, email=${user.email}")
    }

    //навигация в зависимости от роли
    private fun navigateToMainActivity(user: User) {
        val intent = when(user.role) {
            "Клиент" -> Intent(this@SignInActivity, MainClientActivity::class.java)
            "Поставщик" -> Intent(this@SignInActivity, MainSupplierActivity::class.java)
            "Работник" -> Intent(this@SignInActivity, MainWorkerActivity::class.java)
            else -> Intent(this@SignInActivity, MainClientActivity::class.java)
        }

        intent.putExtra("USER_NAME", user.name)
        intent.putExtra("USER_ROLE", user.role)
        intent.putExtra("USER_ID", user.id)

        showSnackBar("Добро пожаловать, ${user.name}!")

        startActivity(intent)
        finish()
    }

    //показ снекбара
    private fun showSnackBar(message: String) {
        runOnUiThread {
            val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.vanilla))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.carob))
            snackbar.show()
        }
    }
}