package com.kelineyt.chatter.feature.chat

import com.kelineyt.chatter.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.kelineyt.chatter.feature.home.HomeViewModel
import com.kelineyt.chatter.feature.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, channelID: String) {
    val viewModel: HomeViewModel = hiltViewModel()
    val channels = viewModel.channels.collectAsState()
    // Find the channel title
    val channelTitle = channels.value
        .firstOrNull { it.id == channelID } // Find the channel with the matching ID
        ?.name // Extract the title
        ?: "Unknown Channel" // Fallback title if the channel is not found
    val systemUiController = rememberSystemUiController()

    // Set status bar color
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Black, // Match scaffold
            darkIcons = false // Ensure icons are visible (white)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = channelTitle,
                        color = Color.White // Ensure title text is visible
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White // Ensure back icon is visible
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black, // Match Scaffold's background
                    titleContentColor = Color.White, // Ensure title is readable
                    navigationIconContentColor = Color.White // Ensure back icon is visible
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val viewModel: ChatViewModel = hiltViewModel()
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessage(channelID)
            }
            val messages = viewModel.messages.collectAsState()
            ChatMessage(
                messages.value,
                onSendMessage = { value ->
                    viewModel.sendMessage(channelID, value)
                }
            )
        }
    }
}


@Composable
fun ChatMessage(
    messages: List<Message>,
    onSendMessage: (String) -> Unit
) {
    val newMessage = remember { mutableStateOf("") }
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(messages) { message ->
                ChatBubble(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .background(Color.LightGray),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = newMessage.value,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        hideKeyboardController?.hide()
                    }),
                onValueChange = {
                    newMessage.value = it
                }
            )

            IconButton(onClick = {
                onSendMessage(newMessage.value)
                newMessage.value = ""
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) Color.Blue else Color.Green
    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = alignment
    ) {
        if (!isCurrentUser) {
            Image(
                painter = painterResource(id = R.drawable.friend),
                contentDescription = "Avatar",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        Box(
            modifier = Modifier
                .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(text = message.message, color = Color.White)
        }
    }
}
