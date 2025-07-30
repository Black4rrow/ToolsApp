package com.example.toolsapp.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.Circle
import com.example.toolsapp.model.classes.CircleGroup
import com.example.toolsapp.model.classes.Difficulty
import com.example.toolsapp.model.classes.Game
import com.example.toolsapp.model.classes.Level
import com.example.toolsapp.ui.theme.HandwritingTypography
import com.example.toolsapp.viewModels.UserGameDataSingleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

enum class GameState {
    RUNNING, PAUSED, STOPPED,
}


class FrenzyClickerGameManager {
    val gameStateFlow = MutableStateFlow(GameState.STOPPED)
    val TEXT_LIFETIME = 800L
    var startTime = 0L
    var playTime = 0L
    var pauseStartTime: Long? = null
    var totalPausedTime = 0L
    var lastPausedTime = 0L
    var maxCombo = 0
    var combo = 0
    var score = 0
    var maxScore = 0
    var highScore = 0
}

var showAreYouBotMessage = false
var areYouBotCount = 0

@Composable
fun FrenzyClickerScreen(onBack: () -> Unit) {
    val gameManager = remember { FrenzyClickerGameManager() }
    val activeCircles = remember { mutableStateListOf<Circle>() }
    val feedbackCircles = remember { mutableStateListOf<Circle>() }
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var difficulty by remember { mutableStateOf(Difficulty.NONE) }

    var showPauseScreen by remember { mutableStateOf(false) }
    var showDifficultyChooser by remember { mutableStateOf(true) }
    var showGameOverScreen by remember { mutableStateOf(false) }

    val frameTicker = remember { mutableLongStateOf(0L) }
    val possibleBonusPoints = 5
    val context = LocalContext.current
    val backGroundColor = MaterialTheme.colorScheme.secondaryContainer
    var lastCircleClicked: Circle? = null

    val areYouBotMessagesId = listOf(
        R.string.are_you_bot_1,
        R.string.are_you_bot_2,
        R.string.are_you_bot_3,
        R.string.are_you_bot_4,
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(difficulty, screenSize) {
        if (difficulty != Difficulty.NONE && screenSize != Size.Zero) {
            resetGame(gameManager, activeCircles = activeCircles, feedbackCircles = feedbackCircles)
            startGame(
                gameManager = gameManager,
                difficulty = difficulty,
                screenSize = screenSize,
                backgroundColor = backGroundColor
            ) { circle ->
                activeCircles.add(circle)
                gameManager.maxScore += 1 * difficulty.value + possibleBonusPoints
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()

            if (gameManager.gameStateFlow.value == GameState.RUNNING) {
                for (circle in activeCircles.toList()) {
                    val lifeElapsed = now - circle.createTimestamp
                    if (lifeElapsed > difficulty.despawnTime + 32 && circle.feedbackText == null) {
                        circle.feedbackText = "X"
                        circle.feedbackTimestamp = now
                        gameManager.combo = 0
                        feedbackCircles.add(circle)
                        activeCircles.remove(circle)
                    }
                }

                for (circle in feedbackCircles.toList()) {
                    circle.feedbackText?.let {
                        val dt = now - circle.feedbackTimestamp
                        if (dt > gameManager.TEXT_LIFETIME) {
                            feedbackCircles.remove(circle)
                        }
                    }
                }

                for (circle in activeCircles.toList()) {
                    circle.remainingTime = (circle.createTimestamp + difficulty.despawnTime - now).coerceAtLeast(0L)

                    if(circle.remainingTime <= difficulty.despawnTime * (1 - 0.99f) && difficulty == Difficulty.BOT){
                        wasCircleClicked(activeCircles, circle.position) { clickedCircle ->
                            activeCircles -= clickedCircle
                            feedbackCircles += clickedCircle

                            if (lastCircleClicked != null) {
                                if (clickedCircle.order == lastCircleClicked!!.order + 1) {
                                    gameManager.combo++
                                } else {
                                    gameManager.combo = 0
                                }
                            }
                            if(gameManager.combo == 0) gameManager.combo++
                            lastCircleClicked = clickedCircle

                            gameManager.score += 1 * difficulty.value +
                                    (possibleBonusPoints * (1f - (clickedCircle.remainingTime / difficulty.despawnTime.toFloat()))).toInt() +
                                    gameManager.combo

                            clickedCircle.feedbackText = when {
                                clickedCircle.remainingTime < difficulty.despawnTime * (1 - 0.99f) -> context.getString(
                                    R.string.wtf
                                )

                                clickedCircle.remainingTime < difficulty.despawnTime * (1 - 0.95f) -> context.getString(
                                    R.string.perfect
                                )

                                clickedCircle.remainingTime < difficulty.despawnTime * (1 - 0.75f) -> context.getString(
                                    R.string.very_good
                                )

                                clickedCircle.remainingTime < difficulty.despawnTime * (1 - 0.60f) -> context.getString(
                                    R.string.good
                                )

                                clickedCircle.remainingTime < difficulty.despawnTime * (1 - 0.40f) -> context.getString(
                                    R.string.okay
                                )

                                else -> context.getString(R.string.huh)
                            }
                            clickedCircle.feedbackTimestamp = System.currentTimeMillis()
                        }
                    }
                }

                gameManager.playTime =
                    System.currentTimeMillis() - gameManager.startTime - gameManager.totalPausedTime
            }

            if (gameManager.gameStateFlow.value == GameState.PAUSED && gameManager.pauseStartTime == null) {
                gameManager.pauseStartTime = now
            }

            frameTicker.longValue = now
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backGroundColor)
    ) {
        Column(modifier = Modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier
                    .align(Alignment.TopStart)
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.combo_prefix) + " ${gameManager.combo}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        style = HandwritingTypography.bodyMedium
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val maxText = stringResource(R.string.max_prefix)
                    Text(
                        modifier = Modifier,
                        text = "${gameManager.score}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        style = HandwritingTypography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        modifier = Modifier,
                        text = "(${maxText} ${gameManager.highScore})",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        style = HandwritingTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row {
                        var difficultyText = stringResource(difficulty.labelKey)
                        if (difficulty == Difficulty.INFINITE || difficulty == Difficulty.BOT) difficultyText += " (" + stringResource(
                            Difficulty.difficultyFromPlayTime(
                                gameManager.playTime
                            ).labelKey
                        ) + ")"
                        Text(
                            modifier = Modifier,
                            text = difficultyText,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            style = HandwritingTypography.bodyMedium
                        )

                        Text(text = " | ", style = HandwritingTypography.bodyMedium)

                        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
                        Text(
                            modifier = Modifier,
                            text = format.format(Date(gameManager.playTime)),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            style = HandwritingTypography.bodyMedium
                        )
                    }
                }

                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable{
                            showPauseScreen = !showPauseScreen
                            if (showDifficultyChooser || showPauseScreen) gameManager.gameStateFlow.value =
                                GameState.PAUSED
                            else gameManager.gameStateFlow.value = GameState.RUNNING

                            if(gameManager.pauseStartTime == null) gameManager.pauseStartTime = System.currentTimeMillis()
                            else resume(gameManager, activeCircles)
                        },
                    imageVector = Icons.Rounded.Pause,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentDescription = "Pause",
                )
            }

            CirclesCanvas(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures { offset: Offset ->
                        if(difficulty == Difficulty.BOT){
                            if(!showAreYouBotMessage){
                                showAreYouBotMessage = true

                                coroutineScope.launch{
                                    areYouBot()
                                }
                            }
                        }

                        wasCircleClicked(activeCircles, offset) { circle ->
                            activeCircles -= circle
                            feedbackCircles += circle

                            if (lastCircleClicked != null) {
                                if (circle.order == lastCircleClicked!!.order + 1) {
                                    gameManager.combo++
                                } else {
                                    gameManager.combo = 0
                                }
                            }
                            if(gameManager.combo == 0) gameManager.combo++
                            lastCircleClicked = circle

                            gameManager.score += 1 * difficulty.value +
                                    (possibleBonusPoints * (1f - (circle.remainingTime / difficulty.despawnTime.toFloat()))).toInt() +
                                    gameManager.combo

                            if(gameManager.score > gameManager.highScore){
                                gameManager.highScore = gameManager.score
                                UserGameDataSingleton.userGameData.frenzyClickerHighScoreMap[difficulty.name] = gameManager.highScore
                            }

                            circle.feedbackText = when {
                                circle.remainingTime < difficulty.despawnTime * (1 - 0.99f) -> context.getString(
                                    R.string.wtf
                                )

                                circle.remainingTime < difficulty.despawnTime * (1 - 0.95f) -> context.getString(
                                    R.string.perfect
                                )

                                circle.remainingTime < difficulty.despawnTime * (1 - 0.75f) -> context.getString(
                                    R.string.very_good
                                )

                                circle.remainingTime < difficulty.despawnTime * (1 - 0.60f) -> context.getString(
                                    R.string.good
                                )

                                circle.remainingTime < difficulty.despawnTime * (1 - 0.40f) -> context.getString(
                                    R.string.okay
                                )

                                else -> context.getString(R.string.huh)
                            }
                            circle.feedbackTimestamp = System.currentTimeMillis()
                        }
                    }
                },
                gameManager = gameManager,
                activeCircles = activeCircles,
                feedbackCircles = feedbackCircles,
                difficulty = difficulty,
                frame = frameTicker.longValue,
                onSizeChanged = { screenSize = it })
        }

        if (showPauseScreen) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(.75f)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .clip(MaterialTheme.shapes.large),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            showPauseScreen = false
                            gameManager.gameStateFlow.value = GameState.RUNNING
                            resume(gameManager, activeCircles)
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.resume),
                            style = HandwritingTypography.bodyMedium
                        )
                    }

                    Button(
                        onClick = {
                            showPauseScreen = false
                            gameManager.gameStateFlow.value = GameState.PAUSED
                            showDifficultyChooser = true
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.change_difficulty),
                            style = HandwritingTypography.bodyMedium
                        )
                    }

                    Button(
                        onClick = {
                            showPauseScreen = false
                            gameManager.gameStateFlow.value = GameState.STOPPED
                            activeCircles.clear()
                            showGameOverScreen = true
                            resetGame(
                                gameManager,
                                activeCircles = activeCircles,
                                feedbackCircles = feedbackCircles
                            )
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.stop_game),
                            style = HandwritingTypography.bodyMedium
                        )
                    }
                }
            }
        }

        if (showGameOverScreen) {
            GameOverScreen(gameManager = gameManager)
        }

        if (showDifficultyChooser && !showPauseScreen) {
            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxSize(.95f)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.choose_difficulty),
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = HandwritingTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    Difficulty.entries.forEach { diff ->
                        if (diff != Difficulty.NONE) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .shadow(8.dp, MaterialTheme.shapes.large),
                                onClick = {
                                    difficulty = diff
                                    showDifficultyChooser = false
                                },
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                )

                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = stringResource(diff.labelKey),
                                    style = HandwritingTypography.labelLarge,
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

        if(showAreYouBotMessage){
            if(areYouBotCount > areYouBotMessagesId.size - 1) areYouBotCount = 0
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(areYouBotMessagesId[areYouBotCount]),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun resume(
    gameManager: FrenzyClickerGameManager,
    activeCircles: SnapshotStateList<Circle>
) {
    gameManager.totalPausedTime += System.currentTimeMillis() - gameManager.pauseStartTime!!
    gameManager.lastPausedTime = System.currentTimeMillis() - gameManager.pauseStartTime!!
    gameManager.pauseStartTime = null
    for (circle in activeCircles.toList()) {
        circle.createTimestamp += gameManager.lastPausedTime
    }
}

@Composable
fun CirclesCanvas(
    gameManager: FrenzyClickerGameManager,
    modifier: Modifier,
    activeCircles: List<Circle>,
    feedbackCircles: List<Circle>,
    difficulty: Difficulty,
    frame: Long,
    onSizeChanged: (Size) -> Unit
) {

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

    Canvas(modifier = modifier
        .fillMaxSize()
        .onSizeChanged { onSizeChanged(it.toSize()) }) {
        val despawnTime =
            if (difficulty != Difficulty.INFINITE && difficulty != Difficulty.BOT) difficulty.despawnTime else Difficulty.difficultyFromPlayTime(
                gameManager.playTime
            ).despawnTime

        if(activeCircles.size > 1){
            for(i in 0 until activeCircles.size - 1){
                drawLine(
                    color = Color.Gray,
                    start = activeCircles[i].position,
                    end = activeCircles[i + 1].position,
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        activeCircles.forEach {
            if (it.feedbackText == null) {
                drawCircle(
                    color = it.color,
                    radius = it.radius,
                    center = it.position,
                )

                if (circleTextPaint != null)
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
        }

        feedbackCircles.forEach {
            it.feedbackText?.let { txt ->
                val dt = (frame - it.feedbackTimestamp).coerceAtLeast(0L)
                val progress = (dt / gameManager.TEXT_LIFETIME.toFloat()).coerceAtMost(1f)

                val yOffset = lerp(-32f, -64f, progress)
                val alpha = (1f - progress)

                drawContext.canvas.nativeCanvas.apply {
                    val paintStroke =
                        feedbackTextPaintStroke.apply { this.alpha = (255 * alpha).toInt() }
                    drawText(
                        txt, it.position.x, it.position.y + yOffset, paintStroke
                    )

                    val paint = feedbackTextPaint.apply { this.alpha = (255 * alpha).toInt() }
                    drawText(
                        txt, it.position.x, it.position.y + yOffset, paint
                    )
                }

                if (progress >= 1f) {
                    it.feedbackText = null
                }
            }
        }
    }
}



@Composable
fun GameOverScreen(
    gameManager: FrenzyClickerGameManager
) {
}

suspend fun areYouBot() {
    delay(2500)
    showAreYouBotMessage = false
    areYouBotCount++
}

suspend fun startGame(
    gameManager: FrenzyClickerGameManager,
    difficulty: Difficulty,
    screenSize: Size,
    backgroundColor: Color,
    addCircle: (Circle) -> Unit
) {
    gameManager.highScore = UserGameDataSingleton.userGameData.frenzyClickerHighScoreMap[difficulty.name] ?: 0
    val game = Game()
    val level: Level = game.getLevel(difficulty, screenSize, backgroundColor)
    gameManager.gameStateFlow.value = GameState.RUNNING
    while (true) {
        if (gameManager.gameStateFlow.value == GameState.STOPPED) break
        waitIfPaused(gameManager.gameStateFlow)

        val nextGroup: Pair<Long, CircleGroup> = if (difficulty == Difficulty.INFINITE || difficulty == Difficulty.BOT) {
            game.getGroup(gameManager.playTime, screenSize, backgroundColor)
        } else {
            level.popNextCircleGroup() ?: break
        }
        val (groupSpawnInterval, circleGroup) = nextGroup

        delayWhileRespectingPause(groupSpawnInterval, gameManager.gameStateFlow)

        while (true) {
            if (gameManager.gameStateFlow.value == GameState.STOPPED) return
            waitIfPaused(gameManager.gameStateFlow)

            val (circleSpawnInterval, circle) = circleGroup.popNextCircle() ?: break
            delayWhileRespectingPause(circleSpawnInterval, gameManager.gameStateFlow)

            circle.createTimestamp = System.currentTimeMillis()
            addCircle(circle)
            gameManager.maxCombo++
            gameManager.maxScore += gameManager.maxCombo
        }
    }
}

private fun resetGame(
    gameManager: FrenzyClickerGameManager,
    activeCircles: SnapshotStateList<Circle>,
    feedbackCircles: SnapshotStateList<Circle>
) {
    gameManager.score = 0
    gameManager.maxScore = 0
    activeCircles.clear()
    feedbackCircles.clear()
    gameManager.startTime = System.currentTimeMillis()
    gameManager.playTime = 0
    gameManager.totalPausedTime = 0
    gameManager.lastPausedTime = 0
    gameManager.pauseStartTime = null
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

fun wasCircleClicked(
    circles: List<Circle>, offset: Offset, onCircleClicked: (Circle) -> Unit
): Boolean {
    for (circle in circles) {
        val dx = offset.x - circle.position.x
        val dy = offset.y - circle.position.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= circle.radius) {
            if (circle.feedbackText != null) return false
            onCircleClicked(circle)
            return true
        }
    }
    return false
}

@Composable
fun rememberTypefaceFromTextStyle(style: TextStyle): Typeface? {
    val resolver = LocalFontFamilyResolver.current
    var typeface by remember(style) { mutableStateOf<Typeface?>(null) }

    LaunchedEffect(resolver, style) {
        val result = resolver.resolve(
            fontFamily    = style.fontFamily,
            fontWeight    = style.fontWeight ?: FontWeight.Normal,
            fontStyle     = style.fontStyle  ?: FontStyle.Normal,
            fontSynthesis = style.fontSynthesis ?: FontSynthesis.All
        )
        typeface = result.value as? Typeface
    }

    return typeface
}

