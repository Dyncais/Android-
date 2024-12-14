package com.example.ihatemobile

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = findViewById<TextView>(R.id.textView)
        val editLog = findViewById<EditText>(R.id.editTextText)
        val editPass = findViewById<EditText>(R.id.editTextTextPassword)
        val button = findViewById<Button>(R.id.button)

        text.setOnClickListener{
            if (text.text == "Нет аккаунта? Создайте")
            {
                button.text = "Создать"
                text.text = "Уже есть аккаунт? Войдите"
            }
            else
            {
                button.text = "Войти"
                text.text = "Нет аккаунта? Создайте";
            }
        }

        button.setOnClickListener {

            val login = editLog.text.toString()
            val password = editPass.text.toString()

            if (text.text == "Нет аккаунта? Создайте")
            {
                lifecycleScope.launch {
                    try {
                        val response = NetworkService.logIn(login, password)
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            val userId = loginResponse?.userId
                            val accessToken = loginResponse?.token?.accessToken
                            val refreshToken = loginResponse?.token?.refreshToken
                            //text.text = "User ID: $userId\nAccess Token: $accessToken\nRefresh Token: $refreshToken"
                            val intent = Intent(this@MainActivity, MainPageActivity::class.java).apply{
                            /*    putExtra("USER_ID", userId)
                                putExtra("ACCESS_TOKEN", accessToken)
                                putExtra("REFRESH_TOKEN", refreshToken)
                            }*/
                            //val intent = Intent(this@MainActivity, ChatsActivity::class.java).apply{
                                putExtra("USER_ID", userId.toString())
                                putExtra("ACCESS_TOKEN", accessToken)
                                putExtra("REFRESH_TOKEN", refreshToken)
                            }
                            startActivity(intent)
                        }
                        else {
                            text.text = "Ошибка входа"
                        }
                    } catch (e: Exception) {
                        text.text = "Ошибка:"
                    }
                }
            }
            else
            {
                lifecycleScope.launch {
                    try {
                        val response = NetworkService.register(login, password)
                        if (response.isSuccessful) {
                            text.text = "Аккаунт создан"
                        }
                        else {
                            text.text = "Ошибка входа"
                        }
                    } catch (e: Exception) {
                        text.text = "Ошибка:"
                    }
                }
            }
        }
    }
}

/*val setUsernameResponse = NetworkService.setUsername("seter", accessToken.toString())

               if (setUsernameResponse.isSuccessful) {
                   text.text = "Имя пользователя успешно обновлено"
                 } else {
                    text.text = "Ошибка при обновлении имени пользователя"
              }*/