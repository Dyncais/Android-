package com.example.ihatemobile

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ChatsActivity : AppCompatActivity(), WebSocketService.WebSocketCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats_page)

        val chatID = intent.getStringExtra("CHAT_ID")?.toIntOrNull() ?: -1
        val accessToken = intent.getStringExtra("ACCESS_TOKEN").toString()

        val messageInput: EditText = findViewById(R.id.input_text)
        val sendButton: Button = findViewById(R.id.send_button)
        val messagesContainer: RecyclerView = findViewById(R.id.messages_container)

        val messages = mutableListOf<Message>()
        val adapter = MessageAdapter(messages)
        messagesContainer.layoutManager = LinearLayoutManager(this)
        messagesContainer.adapter = adapter

        WebSocketService.connectWebSocket(chatID, accessToken, this)


        lifecycleScope.launch {
            try {
                val response = NetworkService.showHistoryChat(accessToken, chatID, 0)
                if (response.isSuccessful) {
                    val receivedMessages = response.body()
                    receivedMessages?.forEach { msg ->
                        messages.add(Message(msg.userName, msg.text))
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("ChatsActivity", "Ошибка загрузки истории сообщений: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatsActivity", "Ошибка при загрузке истории сообщений", e)
            }
        }

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            if (messageText.isNotEmpty()) {
                WebSocketService.sendMessage(messageText)
                messageInput.text.clear()
            }
        }
    }

    override fun onMessageReceived(message: String) {
        Log.d("ChatsActivity", "Получено сообщение: $message")
    }

    override fun onConnectionOpened() {
        Log.d("ChatsActivity", "Соединение установлено")
    }

    override fun onConnectionClosed() {
        Log.d("ChatsActivity", "Соединение закрыто")
    }

    override fun onConnectionFailure(error: String) {
        Log.e("ChatsActivity", "Ошибка подключения: $error")
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketService.closeConnection()
    }


    data class Message(val userName: String, val text: String)


    inner class MessageAdapter(private val messages: List<Message>) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        inner class MessageViewHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView) {
            val messageTextView: TextView = itemView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val textView = TextView(parent.context)
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return MessageViewHolder(textView)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.messageTextView.text = "${message.userName}: ${message.text}"
        }

        override fun getItemCount(): Int {
            return messages.size
        }
    }
}
