package com.example.toolsapp.ui.screens


import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toolsapp.R
import com.example.toolsapp.model.TodoItem
import com.example.toolsapp.ui.components.MyDatePicker
import com.example.toolsapp.viewModels.TodoViewModel
import com.example.toolsapp.viewModels.UserViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val showCreateTaskDialog = remember { mutableStateOf(false) }

    var todoItems by remember { mutableStateOf(emptyList<TodoItem>()) }
    val todoViewModel: TodoViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoViewModel() as T
            }
        }
    )
    val context= LocalActivity.current as ComponentActivity
    val userViewModel = ViewModelProvider(context)[UserViewModel::class.java]
    val userId = userViewModel.getCurrentUserId()

    LaunchedEffect(userId) {
        userId?.let{ uid ->
            todoViewModel.getTodoItems(uid){items ->
                todoItems = items
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            todoItems.forEach { todoItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(.9f)
                        .wrapContentHeight()
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var checked by remember { mutableStateOf(todoItem.isFinished) }

                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                checked = isChecked
                                todoItem.isFinished = isChecked
                                todoViewModel.updateTodoItem(todoItem)
                            },
                            modifier = Modifier
                                .weight(1f)
                        )

                        Text(
                            text = todoItem.title,
                            modifier = Modifier
                                .weight(5f)
                                .wrapContentHeight()
                        )

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item",
                            modifier = Modifier
                                .clickable {
                                    todoViewModel.deleteTodoItem(todoItem.id.toString())
                                }
                                .weight(1f)
                        )
                    }

                    if(todoItem.endDate.isNotEmpty()){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            val currentDate = Calendar.getInstance().time
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                            val endDate = dateFormat.parse(todoItem.endDate)
                            val diffInMillis = endDate?.time?.minus(currentDate.time) ?: 0
                            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                            val textColor = when {
                                endDate != null && endDate.before(currentDate) -> Color.Red
                                diffInDays <= 1 -> Color(0xFFFF8C00)
                                else -> Color.Unspecified
                            }

                            Text(
                                text = "${stringResource(R.string.ends_on)} : ${todoItem.endDate}",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(1f)
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {showCreateTaskDialog.value = true},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(96.dp)
                .padding(16.dp)
        ) {

            Image(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(48.dp)
            )

        }
    }

    if(showCreateTaskDialog.value){
        BasicAlertDialog(
            onDismissRequest = {showCreateTaskDialog.value = false},
            modifier = Modifier
                .requiredSize(288.dp, 512.dp)
//                .background(MaterialTheme.colorScheme.primary)
        ) {
            var taskName by remember { mutableStateOf("") }
            var canTaskExpire by remember { mutableStateOf(false) }
            var taskDate by remember { mutableStateOf(TextFieldValue("")) }


            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Text(text = stringResource(R.string.create_task),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height((512.dp) * 0.75f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = stringResource(R.string.task_name_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W500
                            ))
                        TextField(
                            value = taskName,
                            onValueChange = {taskName = it},
                            placeholder = { Text(text = stringResource(R.string.task_name_example))},
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = stringResource(R.string.task_date_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W500
                            ))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Checkbox(
                                checked = canTaskExpire,
                                onCheckedChange = { canTaskExpire = it },
                            )
                            Text(text = stringResource(R.string.task_can_expire),
                                modifier = Modifier
                                    .align(Alignment.CenterVertically))
                        }

                        if (canTaskExpire) {
                            MyDatePicker(dateParam = taskDate, shouldShow = canTaskExpire, onDateSelected = { taskDate = it })
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    onClick = {
                        showCreateTaskDialog.value = false
                    }
                ){
                    Text(
                        text = stringResource(R.string.cancel),
                        modifier = Modifier,
//                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Button(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    onClick = {
                        showCreateTaskDialog.value = false
                        val task = TodoItem(
                            id = abs(Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).nextInt()),
                            title = taskName,
                            description = "",
                            isFinished = false,
                            endDate = taskDate.text,
                            userId = userViewModel.getCurrentUserId() ?: ""
                        )
                        todoViewModel.addTodoItem(task)
                    }
                ){
                    Text(
                        text = stringResource(R.string.add),
                        modifier = Modifier,
//                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun TodoScreenPreview() {
    TodoScreen(onBack = {})
}