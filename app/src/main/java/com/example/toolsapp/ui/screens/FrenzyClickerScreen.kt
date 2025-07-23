package com.example.toolsapp.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import com.example.toolsapp.R
import com.example.toolsapp.model.advancedShadow
import com.example.toolsapp.model.classes.Circle
import com.example.toolsapp.model.classes.CircleGroup
import com.example.toolsapp.model.classes.Difficulty
import com.example.toolsapp.model.classes.Game
import com.example.toolsapp.model.classes.Level
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

enum class GameState{
    RUNNING,
    PAUSED,
    STOPPED,
}

val gameStateFlow = MutableStateFlow(GameState.STOPPED)
const val TEXT_LIFETIME = 800L
var startTime = 0L
var playTime = 0L
var pauseStartTime:Long? = null
var totalPausedTime = 0L

@Composable
fun FrenzyClickerScreen(onBack: () -> Unit){
    var score by remember { mutableIntStateOf(0) }
    var maxScore by remember { mutableIntStateOf(0) }
    val circles = remember { mutableStateListOf<Circle>() }
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var difficulty by remember { mutableStateOf(Difficulty.NONE) }
    var showDifficultyChooser by remember { mutableStateOf(true) }
    val frameTicker = remember { mutableLongStateOf(0L) }
    val possibleBonusPoints: Int = 5
    val context = LocalContext.current
    val backGroundColor = MaterialTheme.colorScheme.secondaryContainer
    val gameState = gameStateFlow.collectAsState()

    LaunchedEffect(difficulty, screenSize) {
        if (difficulty != Difficulty.NONE && screenSize != Size.Zero) {
            score = 0
            maxScore = 0
            circles.clear()
            startTime = System.currentTimeMillis()
            playTime = 0
            startGame(difficulty, screenSize, backGroundColor) { circle ->
                circles.add(circle)
                maxScore += 1 * difficulty.value + possibleBonusPoints
            }
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            val now = System.currentTimeMillis()

            if (gameStateFlow.value == GameState.RUNNING) {
                if(pauseStartTime != null){
                    totalPausedTime += now - pauseStartTime!!
                    pauseStartTime = null
                }
                for (circle in circles.toList()) {
                    val lifeElapsed = now - circle.createTimestamp
                    if (lifeElapsed > difficulty.despawnTime && circle.feedbackText == null) {
                        circle.feedbackText = "X"
                        circle.feedbackTimestamp = now
                    }
                }

                for (circle in circles.toList()) {
                    circle.feedbackText?.let {
                        val dt = now - circle.feedbackTimestamp
                        if (dt > TEXT_LIFETIME) {
                            circles.remove(circle)
                        }
                    }
                }

                for (circle in circles) {
                    circle.remainingTime = (circle.createTimestamp + difficulty.despawnTime - now)
                        .coerceAtLeast(0L)
                }
                playTime = System.currentTimeMillis() - startTime - totalPausedTime
            }

            if (gameStateFlow.value == GameState.PAUSED && pauseStartTime == null){
                pauseStartTime = now
            }

            frameTicker.longValue = now
            delay(16)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backGroundColor)
        .pointerInput(Unit){
            detectTapGestures{ offset: Offset ->
                wasCircleClicked(circles, offset){ circle ->
                    circles -= circle
                    score += 1 * difficulty.value + (possibleBonusPoints * (1f - (circle.remainingTime / difficulty.despawnTime.toFloat()))).toInt()

                    circle.feedbackText = when{
                        circle.remainingTime < difficulty.despawnTime * (1 - 0.99f) -> context.getString(R.string.wtf)
                        circle.remainingTime < difficulty.despawnTime * (1 - 0.95f) -> context.getString(R.string.perfect)
                        circle.remainingTime < difficulty.despawnTime * (1 - 0.75f) -> context.getString(R.string.very_good)
                        circle.remainingTime < difficulty.despawnTime * (1 - 0.60f) -> context.getString(R.string.good)
                        circle.remainingTime < difficulty.despawnTime * (1 - 0.40f) -> context.getString(R.string.okay)
                        else -> context.getString(R.string.huh)
                    }
                    circle.feedbackTimestamp = System.currentTimeMillis()

                    circles.add(circle)
                }
            }
        }
    ){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier)

            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if (gameState.value == GameState.RUNNING) {
                    Text(modifier = Modifier,
                        text = "Score : $score / $maxScore",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )

                    var difficultyText = stringResource(R.string.difficulty_prefix) + stringResource(difficulty.labelKey)
                    if (difficulty == Difficulty.INFINITE)
                        difficultyText += " (" + stringResource(Difficulty.difficultyFromPlayTime(playTime).labelKey) + ")"
                    Text(modifier = Modifier,
                        text = difficultyText,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )

                    val format = SimpleDateFormat("mm:ss", Locale.getDefault())
                    Text(
                        modifier = Modifier,
                        text = format.format(Date(playTime)),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Icon(
                modifier = Modifier
                    .clickable {
                        showDifficultyChooser = !showDifficultyChooser
                        if(showDifficultyChooser) gameStateFlow.value = GameState.PAUSED
                        else gameStateFlow.value = GameState.RUNNING
                    },
                imageVector = Icons.Filled.Pause,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = "Pause",
            )
        }

        CirclesCanvas(circles, difficulty,frameTicker.longValue ,onSizeChanged = { screenSize = it })

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
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {

                    Difficulty.entries.forEach { diff ->
                        if(diff != Difficulty.NONE){
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .shadow(8.dp, MaterialTheme.shapes.large),
                                onClick = {
                                    difficulty = diff
                                    showDifficultyChooser = false
                                },
                                shape = MaterialTheme.shapes.large
                            ){
                                Text(
                                    modifier = Modifier,
                                    text = stringResource(diff.labelKey),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Icon(
                                    modifier = Modifier.requiredSize(32.dp),
                                    painter = painterResource(diff.iconId),
                                    contentDescription = stringResource(diff.labelKey) + "icon",
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
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

    val circleTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val feedbackTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    val feedbackTextPaintStroke = remember {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 4f
        }
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { onSizeChanged(it.toSize()) }
    ) {
        val despawnTime = if(difficulty != Difficulty.INFINITE) difficulty.despawnTime else Difficulty.difficultyFromPlayTime(playTime).despawnTime

        circles.forEach {
            if (it.feedbackText == null) {
                drawCircle(
                    color = it.color,
                    radius = it.radius,
                    center = it.position,
                )

                drawContext.canvas.nativeCanvas.drawText(
                    it.number.toString(),
                    it.position.x,
                    it.position.y + (circleTextPaint.textSize / 3),
                    circleTextPaint
                )

                val progress = it.remainingTime / despawnTime.toFloat()
                val outerRadius = it.radius * (1f + progress)
                drawCircle(
                    color = it.color,
                    center = it.position,
                    radius = outerRadius,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            it.feedbackText?.let { txt ->
                val dt = (frame - it.feedbackTimestamp).coerceAtLeast(0L)
                val progress = (dt / TEXT_LIFETIME.toFloat()).coerceAtMost(1f)

                val yOffset = lerp(-32f, -64f, progress)
                val alpha   = (1f - progress)

                drawContext.canvas.nativeCanvas.apply {
                    val paintStroke = feedbackTextPaintStroke.apply { this.alpha = (255 * alpha).toInt() }
                    drawText(
                        txt,
                        it.position.x,
                        it.position.y + yOffset,
                        paintStroke
                    )

                    val paint = feedbackTextPaint.apply { this.alpha = (255 * alpha).toInt() }
                    drawText(
                        txt,
                        it.position.x,
                        it.position.y + yOffset,
                        paint
                    )
                }

                if (progress >= 1f) {
                    it.feedbackText = null
                }
            }
        }
    }
}


suspend fun startGame(difficulty: Difficulty,
                      screenSize: Size,
                      backgroundColor: Color,
                      addCircle: (Circle) -> Unit){
    val game = Game()
    val level: Level = game.getLevel(difficulty, screenSize, backgroundColor)
    gameStateFlow.value = GameState.RUNNING
    while (true){
        if (gameStateFlow.value == GameState.STOPPED)
            break
        waitIfPaused(gameStateFlow)

        val nextGroup: Pair<Float, CircleGroup> = if (difficulty == Difficulty.INFINITE) {
            game.getGroup(playTime, screenSize, backgroundColor)
        } else {
            level.popNextCircleGroup() ?: break
        }
        val (groupSpawnInterval, circleGroup) = nextGroup

        delayWhileRespectingPause(groupSpawnInterval.toLong(), gameStateFlow)

        while (true) {
            if (gameStateFlow.value == GameState.STOPPED)
                return
            waitIfPaused(gameStateFlow)

            val (circleSpawnInterval, circle) = circleGroup.popNextCircle() ?: break
            delayWhileRespectingPause(circleSpawnInterval.toLong(), gameStateFlow)

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
            if(circle.feedbackText != null) return false
            onCircleClicked(circle)
            return true
        }
    }
    return false
}

