package com.kelineyt.chatter.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kelineyt.chatter.ui.theme.DarkGrey


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val channels = viewModel.channels.collectAsState()
    val isAddingChannel = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val systemUiController = rememberSystemUiController()

    // Set status bar color
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Black, // Match scaffold
            darkIcons = false // Ensure icons are visible (white)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Blue)
                    .clickable {
                        isAddingChannel.value = true
                    }
            ) {
                Text(
                    "Add channel", modifier = Modifier
                        .padding(16.dp), color = Color.White
                )
            }
        }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn {
                item {
                    Text(
                        text = "Messages",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                item {
                    TextField(
                        value = searchText.value,
                        placeholder = { Text(text = "Search...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        textStyle = TextStyle(color = Color.LightGray),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp), // Apply shape here
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = DarkGrey,
                            unfocusedContainerColor = DarkGrey,
                            focusedTextColor = Color.Gray,
                            unfocusedTextColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedIndicatorColor = Color.Transparent, // Hide the bottom line
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        },
                        onValueChange = {
                            searchText.value = it
                        }
                    )
                }
                items(channels.value) { channel ->
                    ChannelItem(channel.name, onClick = {
                        navController.navigate("chat/${channel.id}")
                    })
                }
            }
        }
    }
    if (isAddingChannel.value) {
        ModalBottomSheet(onDismissRequest = {
            isAddingChannel.value = false
        }, sheetState = sheetState) {
            AddChannelDialog(
                onAddChannel = { text ->
                    viewModel.addChannel(text)
                    isAddingChannel.value = false
                }
            )
        }
    }


}


@Composable
fun ChannelItem(channelName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGrey)
            .clickable(onClick = onClick) // Click effect
            .padding(horizontal = 10.dp, vertical = 10.dp), // Inner padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Yellow.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = channelName.firstOrNull()?.uppercase() ?: "?",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
        }

        // Channel Name
        Text(
            text = channelName,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f), // Makes text take up remaining space
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.White)
        )

        // Forward Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go to Channel",
            tint = Color.White
        )
    }
}


@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    val channelName = remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(16.dp),
//        verticalArrangement = Alignment.Center as Arrangement.Vertical,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Channel")
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(value = channelName.value,
            singleLine = true,
            onValueChange = {
                channelName.value = it
            })
        Spacer(modifier = Modifier.padding(8.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAddChannel(channelName.value)
            }) {
            Text("Add")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChannelItemPreview() {
    ChannelItem("TEST", onClick = {})
}