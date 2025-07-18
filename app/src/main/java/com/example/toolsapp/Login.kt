package com.example.toolsapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.toolsapp.ui.viewModels.UserViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun LoginScreen(
    onLoginSuccess: (userId:String, code:String, userMail: String) -> Unit,
    onLoginFailure: (Exception) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var passwordVisible by remember { mutableStateOf(false) }

        var invalidEmail by remember { mutableStateOf(false) }
        var invalidPassword by remember { mutableStateOf(false) }
        var noNetwork by remember { mutableStateOf(false) }
        var mailErrorText by remember { mutableStateOf("") }
        var passwordErrorText by remember { mutableStateOf("") }

        val invalidMailMessage = stringResource(R.string.invalid_mail_message)
        val weakPasswordMessage = stringResource(R.string.weak_pwd_message)
        val alreadyInUseMessage = stringResource(R.string.mail_already_used_message)

        Column(
            modifier = Modifier
                .fillMaxWidth(.75f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                value = email,
                onValueChange = {
                    email = it
                    invalidEmail = false
                },
                label = { Text(stringResource(R.string.mail_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                ,
                isError = invalidEmail,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                    errorContainerColor = MaterialTheme.colorScheme.secondary,
                )
            )
            if(invalidEmail){
                Text(
                    text = mailErrorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    invalidPassword = false
                },
                label = { Text(stringResource(R.string.pwd_placeholder)) },
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(R.drawable.visibility_off)
                    else painterResource(R.drawable.visibility)

                    IconButton(onClick = {passwordVisible = !passwordVisible}, modifier = Modifier.requiredSize(32.dp)) {
                        Icon(painter = image, contentDescription = "View Password")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                isError = invalidPassword,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                    errorContainerColor = MaterialTheme.colorScheme.secondary,
                )
            )
            if(invalidPassword){
                Text(
                    text = passwordErrorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    noNetwork = false
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        UserViewModel().loginUser(
                            email, password,
                            onSuccess = onLoginSuccess,
                            onFailure = { exception ->
                                onLoginFailure(exception)
                                if (exception is FirebaseAuthException && exception.errorCode == "ERROR_INVALID_EMAIL") {
                                    invalidEmail = true
                                    mailErrorText = invalidMailMessage
                                }else if (exception is FirebaseAuthException && exception.errorCode == "ERROR_WEAK_PASSWORD") {
                                    invalidPassword = true
                                    passwordErrorText = weakPasswordMessage
                                }else if(exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE"){
                                    invalidPassword = true
                                    passwordErrorText = alreadyInUseMessage
                                }else if(exception is FirebaseNetworkException){
                                    noNetwork = true
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(.75f)
            ) {
                Text(text = stringResource(R.string.connect_text))
            }

            if(noNetwork){
                Text(
                    text = stringResource(R.string.no_network_message),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }

            Surface(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(0.dp,31.dp,0.dp,31.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ){}
        }
    }
}