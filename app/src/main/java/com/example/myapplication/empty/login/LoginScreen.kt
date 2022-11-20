package com.example.myapplication.empty.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import recomposeHighlighter

@Composable
fun LoginThunk.LoginScreen() {
    
    val s by state.collectAsState()
    
    val login = remember {
        { dispatch(LoginAction.LogIn) }
    }
    
    val sign: () -> Unit = remember {
        { dispatch(LoginAction.SignIn) }
    }
    
    val nameAction: (String) -> Unit = remember {
        { name: String -> dispatch(LoginAction.ChangeName(name)) }
    }
    
    val passAction: (String) -> Unit = remember {
        { name: String -> dispatch(LoginAction.ChangePassword(name)) }
    }
    
    Content(
        name = s.name,
        password = s.password,
        error = s.error,
        nameAction = nameAction,
        namePass = passAction,
        login = login,
        sign = sign,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Input(
    text: String,
    action: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.recomposeHighlighter(),
        value = text,
        onValueChange = action
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Content(
    name: String,
    password: String,
    error: String?,
    namePass: (String) -> Unit,
    nameAction: (String) -> Unit,
    login: () -> Unit,
    sign: () -> Unit,
) {
    Scaffold(snackbarHost = {
        error?.let {
            Text(text = it)
        }
    }) {
        Column(Modifier.padding(it)) {
            Input(text = name, action = nameAction)
            Spacer(modifier = Modifier.height(10.dp))
            Input(text = password, action = namePass)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                modifier = Modifier.recomposeHighlighter(),
                onClick = login
            ) {
                Text(text = "Login")
            }
            Button(
                modifier = Modifier.recomposeHighlighter(),
                onClick = sign
            ) {
                Text(text = "Sign In")
            }
        }
    }
}