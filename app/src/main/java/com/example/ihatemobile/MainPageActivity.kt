package com.example.ihatemobile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainPageActivity : AppCompatActivity() {

    private var isChatsSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val contactsContainer: LinearLayout = findViewById(R.id.contacts_container)
        val buttonContacts: Button = findViewById(R.id.button_contacts)
        val buttonChats: Button = findViewById(R.id.button_chats)
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)

        buttonContacts.setOnClickListener {
            isChatsSelected = false
            fabAdd.visibility = View.VISIBLE
            loadContacts(contactsContainer)
        }

        buttonChats.setOnClickListener {
            isChatsSelected = true
            fabAdd.visibility = View.VISIBLE
            loadChats(contactsContainer)
        }

        fabAdd.setOnClickListener {
            if (isChatsSelected) {
                showAddChatDialog()
            } else {
                showAddContactDialog()
            }
        }
    }

    private fun loadContacts(contactsContainer: LinearLayout) {
        contactsContainer.removeAllViews()
        lifecycleScope.launch {
            try {
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val response = NetworkService.showContacts(accessToken)

                if (response.isSuccessful) {
                    val contactsList = response.body()
                    contactsList?.forEach { contact ->
                        val contactView = View.inflate(this@MainPageActivity, R.layout.contact_item, null)
                        val contactTextView: TextView = contactView.findViewById(R.id.item_name)
                        val deleteButton: Button = contactView.findViewById(R.id.delete_button)

                        contactTextView.text = contact.login
                        deleteButton.setOnClickListener {
                            deleteContact(contact.id, contactsContainer, contactView)
                        }

                        contactsContainer.addView(contactView)
                    } ?: run {
                        val emptyTextView = TextView(this@MainPageActivity).apply {
                            text = "Список контактов пуст."
                            textSize = 16f
                        }
                        contactsContainer.addView(emptyTextView)
                    }
                } else {
                    displayError("Ошибка при получении контактов: ${response.code()} - ${response.message()}", contactsContainer)
                }
            } catch (e: Exception) {
                displayError("Ошибка сети: ${e.message}", contactsContainer)
            }
        }
    }

    private fun loadChats(chatsContainer: LinearLayout) {
        chatsContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                val userId = intent.getStringExtra("USER_ID").toString()
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val response = NetworkService.allChats(accessToken)

                if (response.isSuccessful) {
                    val chatsList = response.body()
                    chatsList?.forEach { chat ->
                        val chatView = View.inflate(this@MainPageActivity, R.layout.chat_item, null)
                        val chatTextView: TextView = chatView.findViewById(R.id.item_name)
                        val deleteButton: Button = chatView.findViewById(R.id.delete_button)

                        chatTextView.text = "Чат: ${chat.name}, OwnerId: ${chat.ownerId}"

                        chatTextView.setOnClickListener {

                            val intent = Intent(this@MainPageActivity, ChatsActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("ACCESS_TOKEN", accessToken)
                                putExtra("CHAT_ID", chat.id.toString())
                            }
                            startActivity(intent)
                        }

                        deleteButton.setOnClickListener {
                            deleteChat(chat.id, chatsContainer, chatView)
                        }

                        chatsContainer.addView(chatView)
                    } ?: run {
                        val emptyTextView = TextView(this@MainPageActivity).apply {
                            text = "Список чатов пуст."
                            textSize = 16f
                        }
                        chatsContainer.addView(emptyTextView)
                    }
                } else {
                    displayError("Ошибка при получении чатов: ${response.code()} - ${response.message()}", chatsContainer)
                }
            } catch (e: Exception) {
                displayError("Ошибка сети: ${e.message}", chatsContainer)
            }
        }
    }

    private fun showAddChatDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить чат")

        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_chat, null)
        builder.setView(dialogLayout)

        val chatNameInput = dialogLayout.findViewById<TextView>(R.id.chat_name_input)
        val chatUsersInput = dialogLayout.findViewById<TextView>(R.id.chat_users_input)

        builder.setPositiveButton("Добавить") { _, _ ->
            val chatName = chatNameInput.text.toString()
            val chatUsers = chatUsersInput.text.toString()
            addChat(chatName, chatUsers)
        }
        builder.setNegativeButton("Отмена", null)
        builder.show()
    }

    private fun showAddContactDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить контакт")

        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_contact, null)
        builder.setView(dialogLayout)

        val contactNameInput = dialogLayout.findViewById<TextView>(R.id.contact_name_input)

        builder.setPositiveButton("Добавить") { _, _ ->
            val contactName = contactNameInput.text.toString()
            addContact(contactName)
        }
        builder.setNegativeButton("Отмена", null)
        builder.show()
    }

    private fun addChat(chatName: String, chatUsers: String) {
        lifecycleScope.launch {
            try {
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val userIds = chatUsers.split(",").mapNotNull { it.trim().toIntOrNull() }

                val response = NetworkService.createChat(accessToken, chatName, userIds, false)

                if (response.isSuccessful) {
                    loadChats(findViewById(R.id.contacts_container))
                } else {
                    displayError("Ошибка при добавлении чата: ${response.code()} - ${response.message()}", findViewById(R.id.contacts_container))
                }
            } catch (e: Exception) {
                displayError("Ошибка сети при добавлении чата: ${e.message}", findViewById(R.id.contacts_container))
            }
        }
    }

    private fun addContact(contactName: String) {
        lifecycleScope.launch {
            try {
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val response = NetworkService.addContacts(contactName,accessToken)

                if (response.isSuccessful) {
                    loadContacts(findViewById(R.id.contacts_container))
                } else {
                    displayError("Ошибка при добавлении контакта: ${response.code()} - ${response.message()}", findViewById(R.id.contacts_container))
                }
            } catch (e: Exception) {
                displayError("Ошибка сети при добавлении контакта: ${e.message}", findViewById(R.id.contacts_container))
            }
        }
    }

    // Удаление контакта
    private fun deleteContact(contactId: Int, container: LinearLayout, view: View) {
        lifecycleScope.launch {
            try {
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val response = NetworkService.deleteContact(accessToken, contactId)

                if (response.isSuccessful) {
                    container.removeView(view)
                } else {
                    displayError("Ошибка при удалении контакта: ${response.code()} - ${response.message()}", container)
                }
            } catch (e: Exception) {
                displayError("Ошибка сети при удалении контакта: ${e.message}", container)
            }
        }
    }

    // Удаление чата
    private fun deleteChat(chatId: Int, container: LinearLayout, view: View) {
        lifecycleScope.launch {
            try {
                val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()
                val response = NetworkService.deleteChat(accessToken, chatId)

                if (response.isSuccessful) {
                    container.removeView(view)
                } else {
                    displayError("Ошибка при удалении чата: ${response.code()} - ${response.message()}", container)
                }
            } catch (e: Exception) {
                displayError("Ошибка сети при удалении чата: ${e.message}", container)
            }
        }
    }

    private fun displayError(message: String, container: LinearLayout) {
        val errorTextView = TextView(this@MainPageActivity).apply {
            text = message
            textSize = 16f
        }
        container.addView(errorTextView)
    }
}
