package com.example.toolsapp.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.AppSettingsManager
import com.example.toolsapp.ui.theme.colors.levelBarIndicatorBackgroud
import com.example.toolsapp.ui.theme.colors.levelBarIndicatorForeground
import com.example.toolsapp.viewModels.UserViewModel

@Composable
fun ProfileScreen() {
    val context= LocalActivity.current as ComponentActivity
    val userViewModel = ViewModelProvider(context)[UserViewModel::class.java]
    val userData by userViewModel.userData.collectAsState()

    var showEditPseudoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxSize(0.9f)
        ) {
            Row(
            ){
                Text(
                    text = userData.pseudo,
                    style = MaterialTheme.typography.titleLarge.copy(

                    ),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )


                IconButton(
                    onClick = {
                        showEditPseudoDialog = true
                    },
                    modifier = Modifier
                        .requiredSize(28.dp)
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
                ){
                    Icon(
                        painter = painterResource(R.drawable.edit_square),
                        contentDescription = "Edit pseudo",
                    )
                }
            }

            Text(
                text = userData.email,
                style = MaterialTheme.typography.bodySmall.copy(

                ),
                modifier = Modifier
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .align(Alignment.CenterHorizontally)
            ){

                Text(
                    text = userData.level.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 48.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )

                LinearProgressIndicator(
                    progress = { userData.currentXp.toFloat() / userViewModel.getXpToNextLevel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeCap = StrokeCap.Round,
                    color = levelBarIndicatorForeground,
                    trackColor = levelBarIndicatorBackgroud,
                    gapSize = (-12).dp,
                    drawStopIndicator = {}
                )


                Text(
                    text = "${userData.currentXp} / ${userViewModel.getXpToNextLevel()}",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )

            }

            Button(onClick = {
                userViewModel.levelUp(10)
            }){
                Text(text = "Level Up")
            }

            Button(
                onClick = {
                    AppSettingsManager.saveSettings()
                    userViewModel.saveUserDataToDatabase(userViewModel.getCurrentUserId() ?: "",userViewModel.userData.value)
                    userViewModel.signOut()
                },
                modifier = Modifier
            ) {
                Text(
                    stringResource(R.string.disconnect),
                    color = Color.Red
                )
            }
        }
    }

    if(showEditPseudoDialog){
        ChangePseudoDialog(
            onDismiss = { showEditPseudoDialog = false },
            onConfirm = { newPseudo ->
                userViewModel.userData.value.pseudo = newPseudo
                showEditPseudoDialog = false
            }
        )
    }
}

@Composable
fun ChangePseudoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pseudo by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .wrapContentHeight()
        ){
            Text(
                text = stringResource(R.string.popup_change_pseudo),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )

            TextField(
                value = pseudo,
                onValueChange = { newPseudo ->
                    pseudo = newPseudo
                },
                label = { Text(stringResource(R.string.username_label)) },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
//                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
//                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
//                    cursorColor = MaterialTheme.colorScheme.onPrimary
//                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel),
                        modifier = Modifier
                            .padding(start = 8.dp),
                        textAlign = TextAlign.Start,
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                TextButton(onClick = {
                    onConfirm(pseudo)
                }) {
                    Text(text = stringResource(R.string.change),
                        modifier = Modifier
                            .padding(end = 8.dp),
                        textAlign = TextAlign.End,
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}