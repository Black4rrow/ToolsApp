package com.example.toolsapp.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toolsapp.R
import com.example.toolsapp.model.EventTimer
import com.example.toolsapp.ui.components.MyDatePicker
import com.example.toolsapp.ui.components.MyTextFieldColors
import com.example.toolsapp.ui.theme.lightHighlightColor
import com.example.toolsapp.viewModels.EventTimersViewModel
import com.example.toolsapp.viewModels.UserViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EventTimersScreen(onBack: () -> Unit) {
    val notificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    var showCreateEventDialog by remember { mutableStateOf(false) }

    var eventTimers by remember { mutableStateOf(emptyList<EventTimer>()) }
    val eventTimerViewModel: EventTimersViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EventTimersViewModel() as T
            }
        }
    )
    val context = LocalActivity.current as ComponentActivity
    val context2 = LocalContext.current
    val userViewModel = ViewModelProvider(context)[UserViewModel::class.java]
    val userId = userViewModel.getCurrentUserId()
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var selectedEventTimer by remember { mutableStateOf<EventTimer?>(null) }
    var isSelecting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }


    fun reloadEventTimers() {
        userId.let{ uid ->
            eventTimerViewModel.getEventTimers(uid){ items ->
                eventTimers = items
            }
        }
    }

    fun onItemSelected(id: Int){
        selectedIds = if (selectedIds.contains(id)) {
            selectedIds - id
        } else {
            selectedIds + id
        }
        isSelecting = selectedIds.isNotEmpty()
    }

    LaunchedEffect(userId) {
        reloadEventTimers()
    }

    Box(modifier = Modifier.fillMaxSize()){
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(eventTimers, key = { it.id }){ eventTimer ->
                EventCard(
                    eventTimer,
                    isSelected = selectedIds.contains(eventTimer.id),
                    onEventClick = { mode ->
                        // 0 = single click, 1 = long click, 2 = double click
                        if(mode == -1 && isSelecting){
                            onItemSelected(eventTimer.id)
                        }else if(mode == 0){
                            if(!isSelecting){
                                selectedEventTimer = eventTimer
                                showEditEventDialog = true
                            }else{
                                onItemSelected(eventTimer.id)
                            }
                        }else if (mode == 1){
                            onItemSelected(eventTimer.id)
                        }else if (mode == 2){

                        }
                    }
                )
            }
        }

        Button(
            onClick = {showCreateEventDialog = true},
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

    if(showEditEventDialog && selectedEventTimer != null){
        EditEventTimerDialog(
            eventTimer = selectedEventTimer!!,
            onDismiss = {
                showEditEventDialog = false
                selectedEventTimer = null
            },
            onConfirm = { eventTimer ->
                eventTimerViewModel.updateEventTimer(eventTimer)
                reloadEventTimers()
                showEditEventDialog = false
                selectedEventTimer = null
            }
        )
    }

    if(showCreateEventDialog){
        BasicAlertDialog(
            onDismissRequest = {showCreateEventDialog = false},
            modifier = Modifier
                .requiredSize(292.dp, 512.dp)
//                .background(MaterialTheme.colorScheme.primary)
        ) {
            var eventName by remember { mutableStateOf("") }
            var eventDate by remember { mutableStateOf(TextFieldValue("")) }
            val timePickerState = rememberTimePickerState(
                initialHour = 12,
                is24Hour = true
            )

            var dropDownExpanded by remember { mutableStateOf(false) }
            val options: List<String> = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY", "NONE")
            var selectedOption by remember { mutableStateOf("NONE") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Text(text = stringResource(R.string.create_event_timer),
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
                        Text(text = stringResource(R.string.event_name_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W500
                            )
                        )

                        TextField(
                            value = eventName,
                            onValueChange = {eventName = it},
                            placeholder = { Text(text = stringResource(R.string.event_name_example)) },
                            colors = MyTextFieldColors.colors(),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ExposedDropdownMenuBox(
                            expanded = dropDownExpanded,
                            onExpandedChange = { dropDownExpanded = !dropDownExpanded }
                        ) {
                            TextField(
                                value = getOptionLabel(selectedOption),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.loop_mode_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded) },
                                colors = MyTextFieldColors.colors(),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = dropDownExpanded,
                                onDismissRequest = { dropDownExpanded = false },
                            ){
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(getOptionLabel(option)) },
                                        onClick = {
                                            selectedOption = option
                                            dropDownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        MyDatePicker(
                            dateParam = eventDate,
                            shouldShow = true,
                            onDateSelected = { eventDate = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TimeInput(
                            timePickerState,
//                            colors = TimePickerDefaults.colors(
//                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiary,
//                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    onClick = {
                        showCreateEventDialog = false
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
                        showCreateEventDialog = false
                        val event = EventTimer(
                            id = abs(Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).nextInt()),
                            title = eventName,
                            loopMode = selectedOption,
                            endDate = eventDate.text,
                            endHour = timePickerState.hour.toString(),
                            endMinute = timePickerState.minute.toString(),
                            userId = userViewModel.getCurrentUserId() ?: ""
                        )
                        eventTimerViewModel.addEventTimer(event)
                        eventTimerViewModel.createEventTimerNotification(context2, event)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventTimerDialog(
    eventTimer: EventTimer,
    onDismiss: () -> Unit,
    onConfirm: (EventTimer) -> Unit
){
    var titleText by remember { mutableStateOf(eventTimer.title) }
    var eventDate by remember { mutableStateOf(TextFieldValue(eventTimer.endDate)) }
    val timePickerState = rememberTimePickerState(
        initialHour = eventTimer.endHour.toInt(),
        initialMinute = eventTimer.endMinute.toInt(),
        is24Hour = true
    )

    var dropDownExpanded by remember { mutableStateOf(false) }
    val options: List<String> = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY", "NONE")
    var selectedOption by remember { mutableStateOf("NONE") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight(.9f)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                TextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text(text = eventTimer.title) },
                    label = { Text(text = stringResource(R.string.event_name_label)) },
                    colors = MyTextFieldColors.colors(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(
                    expanded = dropDownExpanded,
                    onExpandedChange = { dropDownExpanded = !dropDownExpanded }
                ) {
                    TextField(
                        value = getOptionLabel(selectedOption),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.loop_mode_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded) },
                        colors = MyTextFieldColors.colors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = dropDownExpanded,
                        onDismissRequest = { dropDownExpanded = false },
                    ){
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(getOptionLabel(option)) },
                                onClick = {
                                    selectedOption = option
                                    dropDownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                MyDatePicker(
                    dateParam = eventDate,
                    shouldShow = true,
                    onDateSelected = { eventDate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                TimeInput(
                    timePickerState,
//                    colors = TimePickerDefaults.colors(
//                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiary,
//                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(8.dp),
                    onClick = {
                        onDismiss()
                    }
                ) {
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
                        .padding(8.dp),
                    onClick = {
                        val updatedEventTimer = eventTimer.copy(
                            title = titleText,
                            loopMode = selectedOption,
                            endDate = eventDate.text,
                            endHour = timePickerState.hour.toString(),
                            endMinute = timePickerState.minute.toString()
                        )
                        onConfirm(updatedEventTimer)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.edit),
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

@Composable
fun EventCard(
    eventTimer: EventTimer,
    isSelected: Boolean,
    onEventClick: (clickMode: Int) -> Unit
){
    var remainingTime by remember { mutableStateOf(eventTimer.getRemainingTime()) }

    val millisInDay = 24 * 60 * 60 * 1000
    val millisInHour = 60 * 60 * 1000
    val millisInMinute = 60 * 1000

    val days = remainingTime / millisInDay
    val hours = (remainingTime % millisInDay) / millisInHour
    val minutes = (remainingTime % millisInHour) / millisInMinute
    val seconds = (remainingTime % millisInMinute) / 1000

    val context = LocalContext.current
    val evenTimerViewModel: EventTimersViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EventTimersViewModel() as T
            }
        }
    )

    LaunchedEffect(key1 = evenTimerViewModel.aTimerWasUpdated) {
        evenTimerViewModel.resetUpdateFlag()
        while(remainingTime > 0){
            kotlinx.coroutines.delay(1000)
            remainingTime = eventTimer.getRemainingTime()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)
            .clip(MaterialTheme.shapes.medium)
            .clickable {  }
            .combinedClickable (
                onClick = { onEventClick (0)},
                onLongClick = { onEventClick (1) },
                onDoubleClick = { onEventClick (2) }
            )
            .padding(0.dp),
        border = if(isSelected) BorderStroke(4.dp, lightHighlightColor) else null,
//        colors = CardDefaults.cardColors(
//            containerColor = if(remainingTime > 0)
//                MaterialTheme.colorScheme.primaryContainer
//            else
//                MaterialTheme.colorScheme.primaryContainer.copy(
//                    red = MaterialTheme.colorScheme.primaryContainer.red * 1.2f
//                ),
//            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = eventTimer.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(

                )
            )

            Text(
                text = stringResource(R.string.remaining_time) +
                        " : " +
                        (if (days > 0) "$days ${stringResource(R.string.days).lowercase()}," else "") +
                        (if (hours > 0) " $hours ${stringResource(R.string.hours).lowercase()}," else "") +
                        (if (minutes > 0) " $minutes ${stringResource(R.string.minutes).lowercase()}," else "") +
                        " $seconds ${stringResource(R.string.seconds).lowercase()}",
                modifier = Modifier
                    .wrapContentHeight(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun getOptionLabel(option: String): String {
    return when(option) {
        "DAILY" -> stringResource(R.string.every_day)
        "WEEKLY" -> stringResource(R.string.every_week)
        "MONTHLY" -> stringResource(R.string.every_month)
        "YEARLY" -> stringResource(R.string.every_year)
        "NONE" -> stringResource(R.string.dont_loop)
        else -> option
    }
}