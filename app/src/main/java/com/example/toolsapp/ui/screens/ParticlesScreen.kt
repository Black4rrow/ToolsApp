package com.example.toolsapp.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.toolsapp.R
import com.example.toolsapp.model.topBorder
import com.example.toolsapp.ui.components.MyTextFieldColors
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ParticleScreen(onBack: () -> Unit) {
    var pickedColor by remember { mutableStateOf(Color.Red) }
    var showChangeColorDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val containerSize = remember { mutableStateOf(IntSize.Zero) }
    val random = remember { Random(System.currentTimeMillis()) }

    val settings = remember {
        mutableStateOf(
            ParticleSettings(
                speed = 8,
                size = 2,
                particlesGlow = false,
                offScreenMode = OffScreenMode.BOUNCE,
                maxParticles = 10000,
                randomColor = false,
                spawnFrequency = 5
            )
        )
    }

    val particlesState = remember { mutableStateOf<List<Particle>>(emptyList()) }
    val frameUpdateTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                val updatedParticles = mutableListOf<Particle>()
                for (p in particlesState.value) {
                    var newX = p.x + p.dx
                    var newY = p.y + p.dy
                    var newDx = p.dx
                    var newDy = p.dy

                    when (settings.value.offScreenMode) {
                        OffScreenMode.DESTROY -> {
                            if (newX < 0f || newX > containerSize.value.width ||
                                newY < 0f || newY > containerSize.value.height
                            ) continue
                        }
                        OffScreenMode.LOOP -> {
                            if (newX < 0f) newX = containerSize.value.width.toFloat()
                            if (newX > containerSize.value.width) newX = 0f
                            if (newY < 0f) newY = containerSize.value.height.toFloat()
                            if (newY > containerSize.value.height) newY = 0f
                        }
                        OffScreenMode.BOUNCE -> {
                            if (newX < 0f || newX > containerSize.value.width) newDx *= -1
                            if (newY < 0f || newY > containerSize.value.height) newDy *= -1
                        }
                    }

                    updatedParticles.add(
                        p.copy(
                            x = newX.coerceIn(0f, containerSize.value.width.toFloat()),
                            y = newY.coerceIn(0f, containerSize.value.height.toFloat()),
                            dx = newDx,
                            dy = newDy
                        )
                    )
                }
                particlesState.value = updatedParticles
                frameUpdateTrigger.value++
            }
            delay(16L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val downEvent = awaitPointerEvent()
                                val down = downEvent.changes.firstOrNull { it.pressed }
                                if (down != null) {
                                    val pointerId = down.id
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == pointerId }
                                        if (change == null || !change.pressed) break
                                        if (particlesState.value.size < settings.value.maxParticles) {
                                            val position = change.position
                                            val color = if (settings.value.randomColor) {
                                                Color(
                                                    red = random.nextInt(0, 256),
                                                    green = random.nextInt(0, 256),
                                                    blue = random.nextInt(0, 256)
                                                )
                                            } else pickedColor

                                            val newParticles = List(settings.value.spawnFrequency) {
                                                val angle = random.nextFloat() * 2 * PI.toFloat()
                                                Particle(
                                                    x = position.x,
                                                    y = position.y,
                                                    dx = cos(angle) * settings.value.speed,
                                                    dy = sin(angle) * settings.value.speed,
                                                    color = color
                                                )
                                            }

                                            particlesState.value += newParticles
                                            frameUpdateTrigger.value++
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .onGloballyPositioned { containerSize.value = it.size }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val particles = particlesState.value
                    val size = settings.value.size.toFloat()
                    val showGlow = settings.value.particlesGlow

                    val glowPaint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                    }

                    drawIntoCanvas { canvas ->
                        for ((index, particle) in particles.withIndex()) {
                            val center = Offset(particle.x, particle.y)

                            if (showGlow
//                                && index % 2 == 0
                            ) {
                                glowPaint.color = particle.color.copy(alpha = 0.7f).toArgb()
                                glowPaint.maskFilter = BlurMaskFilter(
                                    size * 6,
                                    BlurMaskFilter.Blur.NORMAL
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    glowPaint.blendMode = android.graphics.BlendMode.PLUS
                                }

                                canvas.nativeCanvas.drawCircle(
                                    center.x,
                                    center.y,
                                    size * 2.5f,
                                    glowPaint
                                )
                            }

                            drawCircle(
                                color = particle.color,
                                radius = size / 2f,
//                                radius = 5f,
                                center = center,
                                blendMode = BlendMode.Plus,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f)
                    .background(Color.Black)
                    .topBorder(2.dp, Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .requiredSize(42.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(pickedColor)
                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        .clickable { showChangeColorDialog = true }
                        .align(Alignment.CenterVertically)
                )

                IconButton(
                    onClick = {
                        particlesState.value = emptyList()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.refresh_icon),
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.picture_icon),
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.settings_icon),
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showChangeColorDialog) {
        ChangeColorDialog(
            initialColor = pickedColor,
            onColorChange = { pickedColor = it },
            onDismiss = { showChangeColorDialog = false },
            onConfirm = {
                pickedColor = it
                showChangeColorDialog = false
            },
            settings = settings
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onConfirm = { showSettingsDialog = false },
            settings = settings
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    settings: MutableState<ParticleSettings>
){
    var dropDownExpanded by remember { mutableStateOf(false) }
    val options = OffScreenMode.entries


    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.9f
            ),
            tonalElevation = 8.dp
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 320.dp, max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .weight(.9f),
                ) {
                    Text(
                        stringResource(R.string.particles_settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.speed))
                    MySlider(
                        value = settings.value.speed.toFloat(),
                        onValueChange = {
                            settings.value = settings.value.copy(speed = it.toInt())
                        },
                        valueRange = 1f..20f,
                        steps = 100,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.size))
                    MySlider(
                        value = settings.value.size.toFloat(),
                        onValueChange = { settings.value = settings.value.copy(size = it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 100
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.particles_glow))
                    Switch(
                        checked = settings.value.particlesGlow,
                        onCheckedChange = {
                            settings.value = settings.value.copy(particlesGlow = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.when_offscreen))
                    ExposedDropdownMenuBox(
                        expanded = dropDownExpanded,
                        onExpandedChange = { dropDownExpanded = !dropDownExpanded }
                    ) {
                        TextField(
                            value = getLabelFromOffScreenMode(settings.value.offScreenMode),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.when_offscreen)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded) },
                            colors = MyTextFieldColors.colors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = dropDownExpanded,
                            onDismissRequest = { dropDownExpanded = false },
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(getLabelFromOffScreenMode(option)) },
                                    onClick = {
                                        settings.value = settings.value.copy(offScreenMode = option)
                                        dropDownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.max_particles) + " : " + if (settings.value.maxParticles >= 100000) "∞" else settings.value.maxParticles.toString())
                    MySlider(
                        value = settings.value.maxParticles.toFloat(),
                        onValueChange = {
                            if(it.toInt() >= 100000) settings.value = settings.value.copy(maxParticles = Int.MAX_VALUE)
                            else settings.value = settings.value.copy(maxParticles = it.toInt())
                        },
                        valueRange = 100f..100000f,
                        steps = 9900
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.spawn_frequency))
                    MySlider(
                        value = settings.value.spawnFrequency.toFloat(),
                        onValueChange = {
                            settings.value = settings.value.copy(spawnFrequency = it.toInt())
                        },
                        valueRange = 1f..10f,
                        steps = 9
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(.1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
                }
            }
        }
    }
}

@Composable
fun ChangeColorDialog(
    initialColor: Color,
    onColorChange: (Color) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
    settings: MutableState<ParticleSettings>
){
    val controller = rememberColorPickerController()
    var color by remember { mutableStateOf(Color.Transparent) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(.9f)
                    .verticalScroll(rememberScrollState())
            ){
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        onColorChange(colorEnvelope.color)
                        color = colorEnvelope.color
                    },
                    initialColor = initialColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                BrightnessSlider(
                    controller = controller,
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .height(24.dp)
                        .align(Alignment.CenterHorizontally),
                    wheelRadius = 6.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Absolute.SpaceAround, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()) {
                    Text(stringResource(R.string.random_color))
                    Switch(
                        checked = settings.value.randomColor,
                        onCheckedChange = { settings.value = settings.value.copy(randomColor = it) }
                    )
                }
            }

            Button(
                onClick = {onConfirm(color)},
                modifier = Modifier
                    .fillMaxWidth(.5f)
                    .weight(.1f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val dx: Float,
    val dy: Float,
    val color: Color,
)

data class ParticleSettings(
    val speed: Int,
    val size: Int,
    val particlesGlow: Boolean,
    val offScreenMode: OffScreenMode,
    val maxParticles: Int,
    val randomColor: Boolean,
    val spawnFrequency: Int
)

enum class OffScreenMode{
    DESTROY,
    LOOP,
    BOUNCE
}

@Composable
fun getLabelFromOffScreenMode(offScreenMode: OffScreenMode): String{
    return when(offScreenMode){
        OffScreenMode.DESTROY -> stringResource(R.string.destroy)
        OffScreenMode.LOOP -> stringResource(R.string.loop)
        OffScreenMode.BOUNCE -> stringResource(R.string.bounce)
    }
}

@Composable
fun MySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
){
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.onPrimary,
            inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
    )
}

