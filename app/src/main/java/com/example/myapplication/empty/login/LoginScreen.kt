package com.example.myapplication.empty.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapplication.empty.LoginAction
import com.example.myapplication.empty.LoginThunk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginThunk.LoginScreen() {

    val s by state.collectAsState()

    Scaffold() {
        Column(Modifier.padding(it)) {
            TextField(value = s.name, onValueChange = { dispatch(LoginAction.ChangeName(it)) })
            Button(onClick = { dispatch(LoginAction.Send) }) {
                Text(text = "Login")
            }
        }
    }
}