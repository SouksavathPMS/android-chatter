package com.kelineyt.chatter.feature.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.kelineyt.chatter.feature.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    val db = Firebase.database
    val currentUser = Firebase.auth.currentUser

    fun sendMessage(channelID: String, messageText: String) {
        // Validate user and input
        if (currentUser == null) {
            println("User not logged in")
            return
        }
        if (messageText.isBlank()) {
            println("Message text cannot be empty")
            return
        }

        // Generate a unique key for the message
        val messageKey = db.reference.child("messages").child(channelID).push().key
            ?: throw IllegalStateException("Failed to generate message key")

        // Create the message object
        val message = Message(
            messageKey,
            currentUser.uid,
            messageText,
            System.currentTimeMillis(),
            currentUser.displayName ?: "",
            null,
            null
        )

        // Save the message to Firebase
        db.reference.child("messages").child(channelID).child(messageKey).setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Message sent successfully!")
                } else {
                    println("Failed to send message: ${task.exception?.message}")
                }
            }
    }


    fun listenForMessage(channelID: String) {
        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                snapshot.children.forEach { data ->
                    val message = data.getValue(Message::class.java)
                    message?.let {
                        list.add(it)
                    }
                }
                _messages.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                println("Database error: ${error.message}")
            }
        }
        db.getReference("messages").child(channelID).orderByChild("createdAt")
            .addValueEventListener(messageListener)
    }
}