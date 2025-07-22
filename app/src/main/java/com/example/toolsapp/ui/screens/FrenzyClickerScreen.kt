package com.example.toolsapp.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.Circle
import com.example.toolsapp.model.classes.Difficulty
import com.example.toolsapp.model.classes.Game
import com.example.toolsapp.model.classes.Level
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

enum class GameState{
    RUNNING,
    PAUSED,
    STOPPED,
}

@Composable
fun FrenzyClickerScreen(onBack: () -> Unit){
    var score by remember { mutableIntStateOf(0) }
    var maxScore by remember { mutableIntStateOf(0) }
    var circles = remember { mutableStateListOf<Circle>() }
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var difficulty by remember { mutableStateOf(Difficulty.NONE) }
    var showDifficultyChooser by remember { mutableStateOf(true) }
    val frameTicker = remember { mutableStateOf(0L) }
    var possibleBonusPoints: Int = 5

    val gameState = MutableStateFlow(GameState.RUNNING)


    //TEST
    val color = MaterialTheme.colorScheme.onSecondaryContainer

    LaunchedEffect(difficulty, screenSize) {
        if (difficulty != Difficulty.NONE && screenSize != Size.Zero) {
            score = 0
            maxScore = 0
            startGame(difficulty, screenSize, gameState) { circle ->
                circles.add(circle)
                maxScore += 1 * difficulty.value + possibleBonusPoints
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            circles.removeAll { now - it.createTimestamp > difficulty.despawnTime }
            circles.forEach {
                it.remainingTime = difficulty.despawnTime - (now - it.createTimestamp)
            }
            frameTicker.value = now
            delay(16)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .pointerInput(Unit){
            detectTapGestures{ offset: Offset ->
                wasCircleClicked(circles, offset){ circle ->
                    circles -= circle
                    score += 1 * difficulty.value + (possibleBonusPoints * (1f - (circle.remainingTime / difficulty.despawnTime.toFloat()))).toInt()
                }
            }
        }
    ){

        Text(modifier = Modifier
            .align(Alignment.TopCenter),
            text = "Score : $score / $maxScore",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    showDifficultyChooser = true
                    gameState.value = GameState.STOPPED
                },
            imageVector = Icons.Filled.Pause,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            contentDescription = "Pause",
        )

        CirclesCanvas(circles, difficulty,frameTicker.value ,onSizeChanged = { screenSize = it })

        if (showDifficultyChooser){
            Column(modifier = Modifier.fillMaxSize(.8f).align(Alignment.Center), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.choose_difficulty),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Difficulty.entries.forEach { diff ->
                        if(diff != Difficulty.NONE){
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    difficulty = diff
                                    showDifficultyChooser = false
                                }
                            ){
                                Text(
                                    modifier = Modifier,
                                    text = stringResource(diff.labelKey),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CirclesCanvas(circles: List<Circle>,
                  difficulty: Difficulty,
                  frame: Long,
                  onSizeChanged: (Size) -> Unit) {

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { onSizeChanged(it.toSize()) }
    ) {
        circles.forEach {
            drawCircle(
                color = it.color,
                radius = it.radius,
                center = it.position,
            )

            drawContext.canvas.nativeCanvas.drawText(
                it.number.toString(),
                it.position.x,
                it.position.y + (textPaint.textSize / 3),
                textPaint
            )

            val progress = it.remainingTime / difficulty.despawnTime.toFloat()
            val outerRadius = it.radius * (1f + progress)
            drawCircle(
                color = it.color,
                center = it.position,
                radius = outerRadius,
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}


suspend fun startGame(difficulty: Difficulty, screenSize: Size, gameState: StateFlow<GameState> ,addCircle: (Circle) -> Unit){

    val game = Game()
    val level: Level = game.getLevel(difficulty, screenSize)
    while (true){
        if (gameState.value == GameState.STOPPED) break
        waitIfPaused(gameState)

        val (groupSpawnInterval, circleGroup) = level.popNextCircleGroup() ?: break
        delayWhileRespectingPause(groupSpawnInterval.toLong(), gameState)

        while (true) {
            if (gameState.value == GameState.STOPPED) return
            waitIfPaused(gameState)

            val (circleSpawnInterval, circle) = circleGroup.popNextCircle() ?: break
            delayWhileRespectingPause(circleSpawnInterval.toLong(), gameState)

            circle.createTimestamp = System.currentTimeMillis()
            addCircle(circle)
        }
    }
}

suspend fun waitIfPaused(gameState: StateFlow<GameState>) {
    while (gameState.value == GameState.PAUSED) {
        delay(100)
    }
}

suspend fun delayWhileRespectingPause(duration: Long, gameState: StateFlow<GameState>) {
    val start = System.currentTimeMillis()
    var elapsed: Long
    do {
        waitIfPaused(gameState)
        delay(50)
        elapsed = System.currentTimeMillis() - start
    } while (elapsed < duration)
}

fun wasCircleClicked(circles: List<Circle>, offset: Offset, onCircleClicked:(Circle) -> Unit): Boolean {
    for (circle in circles) {
        val dx = offset.x - circle.position.x
        val dy = offset.y - circle.position.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= circle.radius) {
            onCircleClicked(circle)
            return true
        }
    }
    return false
}

