package com.kelineyt.chatter.feature.home

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.Log
import com.google.firebase.database.database
import com.kelineyt.chatter.feature.model.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannel()
    }

    private fun getChannel() {
        firebaseDatabase.getReference("channel").get().addOnSuccessListener { dataSnapshot ->
            val list = mutableListOf<Channel>()
            dataSnapshot.children.forEach { data ->
                val key = data.key ?: return@forEach // Skip if key is null
                val value = data.value?.toString() ?: return@forEach // Skip if value is null
                val channel = Channel(key, value)
                list.add(channel)
            }
            _channels.value = list
        }
    }

    fun addChannel(channelName: String) {
        val key = firebaseDatabase.getReference("channel").push().key
        firebaseDatabase.getReference("channel").child(key!!).setValue(channelName)
            .addOnCompleteListener {
                getChannel()
            }

    }
}