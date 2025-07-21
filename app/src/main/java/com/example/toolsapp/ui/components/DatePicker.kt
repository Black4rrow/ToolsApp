package com.example.toolsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.toolsapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePicker(dateParam: TextFieldValue = TextFieldValue(""), shouldShow: Boolean, onDateSelected: (TextFieldValue) -> Unit){
    var date by remember { mutableStateOf(dateParam) }
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDate by remember { mutableStateOf<Long?>(null) }

    var lastValue = ""
    fun formatDateInput(newInput: String): TextFieldValue {
        val baseText = newInput.take(10)
        val digits = baseText.filter { it.isDigit() }
        val sb = StringBuilder(baseText)

        if ((digits.length == 2 || digits.length == 4) && newInput.length > lastValue.length) {
            sb.append('/')
        }

        val formattedText = sb.toString()
        lastValue = formattedText
        return TextFieldValue(
            text = formattedText,
            selection = TextRange(formattedText.length)
        )
    }

    TextField(
        value = date,
        onValueChange = { newValue ->
            date = formatDateInput(newValue.text)
            onDateSelected(date)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = shouldShow) { showDatePicker = true },
        enabled = shouldShow,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text(stringResource(R.string.select_date)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.select_date),
                modifier = Modifier.clickable { showDatePicker = true }
            )
        },
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
//            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
//            focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
//            cursorColor = MaterialTheme.colorScheme.onPrimary,
//            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
//            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
//        ),
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = TextFieldValue(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis)),
                            selection = TextRange(10)
                        )
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            androidx.compose.material3.DatePicker(
                state = datePickerState,
                title = { Text(
                    stringResource(R.string.select_task_date),
                    modifier = Modifier.padding(16.dp)) }
            )
        }
    }
}
