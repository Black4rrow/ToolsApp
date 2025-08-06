package com.example.toolsapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.Cell
import com.example.toolsapp.model.classes.Grid
import com.example.toolsapp.model.classes.Bullet
import com.example.toolsapp.model.classes.Cannon
import com.example.toolsapp.model.classes.Goliath
import com.example.toolsapp.model.classes.Burst
import com.example.toolsapp.model.classes.TurretInterface
import com.example.toolsapp.ui.theme.darkManaColor
import com.example.toolsapp.ui.theme.manaColor
import com.example.toolsapp.ui.theme.onManaColor
import com.example.toolsapp.ui.theme.selectedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

var deltaTime: Float = 0f

enum class Team(val color: Color) {
    RED(Color(0xFFC22525)),
    BLUE(Color(0xFF3131CD))
}

enum class Face{
    NORTH,
    SOUTH,
    EAST,
    WEST,
}

enum class TurretTypes{
    CANNON,
    GOLIATH,
    BURST
}

enum class GridModes{
    TWO_PLAYER,
    SINGLE_PLAYER
}

enum class GridDifficulty(
    val titleId: Int,
    val botPlayTime: Long
){
    EASY(R.string.easy, 10000L),
    MEDIUM(R.string.medium, 8000L),
    HARD(R.string.hard, 7000L),
    IMPOSSIBLE(R.string.impossible, 6000L),
    DEMON(R.string.demon, 5000L),
}

data class TurretEntry(
    val turret: TurretInterface,
    val weight: Int
)

object GameData{
    var actualColumns = 0
    var actualRows = 0
    var cellSize = 0f
    var gridOffsetX = 0f
    var gridOffsetY = 0f
    var gridWidth = 0f
    var gridHeight = 0f

    var numberOfColumn = 50
    val intervalSize = 1f
    var grid: Grid? = null
    var gridSize: IntSize = IntSize.Zero

    var currentPlayerMana by mutableFloatStateOf(4f)
    var currentEnemyMana by mutableFloatStateOf(4f)
    val globalMaxMana: Float = 20f
    var playerManaGrowRate: Float = 2f
    var enemyManaGrowRate: Float = 2f

    var allBullets = arrayListOf<Bullet>()
    var placedTurrets = arrayListOf<TurretInterface>()

    var playerSelectedTurretType: TurretTypes? = null
    var enemySelectedTurretType: TurretTypes? = null
    var playerSelectedIndex = -1
    var enemySelectedIndex = -1
    val turretsCooldown = 4000L
    var playerPercentage by mutableFloatStateOf(0f)
    var enemyPercentage by mutableFloatStateOf(0f)

    val gameStateFlow = MutableStateFlow(GameState.STOPPED)
    var playerTeam: Team = Team.BLUE
    var enemyTeam: Team = Team.RED

    var debugMode by mutableStateOf(false)
    var mode by mutableStateOf(GridModes.TWO_PLAYER)
    var difficulty by mutableStateOf(GridDifficulty.EASY)
    var lastBotActionTime = System.currentTimeMillis()

    private var numberOfClickOnManaBar = 0
    private var lastManaBarClickTime = 0L
    var showStartScreen by mutableStateOf(true)
    var showPauseScreen by mutableStateOf(false)
    fun addManaBarClick(){
        if(System.currentTimeMillis() - lastManaBarClickTime < 1000)
            numberOfClickOnManaBar++
        else
            numberOfClickOnManaBar = 1


        if(numberOfClickOnManaBar >= 5) {
            gameStateFlow.value = GameState.PAUSED
            showPauseScreen = true
        }
        lastManaBarClickTime = System.currentTimeMillis()
    }

    fun generateNewGrid(canvasWidth: Int, canvasHeight: Int): Grid? {
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return null
        }

        val minRequiredWidth = (2 * intervalSize + 10).toInt() // 10 = taille minimale d'une cellule
        val minRequiredHeight = (2 * intervalSize + 10).toInt()

        if (canvasWidth < minRequiredWidth || canvasHeight < minRequiredHeight) {
            return null
        }

        val availableWidth = canvasWidth - (numberOfColumn + 1) * intervalSize

        if (availableWidth <= 0) {
            return null
        }

        val idealCellSize = availableWidth / numberOfColumn

        cellSize = maxOf(1f, if (idealCellSize - idealCellSize.toInt() < 0.5f) {
            idealCellSize.toInt().toFloat()
        } else {
            (idealCellSize.toInt() + 1).toFloat()
        })

        val totalCellWidth = cellSize + intervalSize
        actualColumns = maxOf(1, ((canvasWidth - intervalSize) / totalCellWidth).toInt())
        actualRows = maxOf(1, ((canvasHeight - intervalSize) / totalCellWidth).toInt())

        if (actualRows < 2) {
            actualRows = 2
        } else if (actualRows % 2 == 1) {
            actualRows -= 1
        }

        if (actualColumns <= 0 || actualRows <= 0) {
            return null
        }

        gridWidth = actualColumns * cellSize + (actualColumns + 1) * intervalSize
        gridHeight = actualRows * cellSize + (actualRows + 1) * intervalSize
        gridOffsetX = (canvasWidth - gridWidth) / 2f
        gridOffsetY = (canvasHeight - gridHeight) / 2f

        val cells: Array<Array<Cell>> = Array(actualRows) { Array(actualColumns) {
            Cell(Team.RED, Offset.Zero, maxOf(1, (cellSize / 2).toInt()))
        }}

        for (i in 0 until actualRows) {
            for (j in 0 until actualColumns) {
                cells[i][j] = Cell(
                    team = if (i < actualRows / 2) enemyTeam else playerTeam,
                    position = getCellPosition(i, j),
                    radius = maxOf(1, (cellSize / 2).toInt())
                )
            }
        }

        gridSize = IntSize(gridWidth.toInt(), gridHeight.toInt())

        return Grid(
            width = gridWidth.toInt(),
            height = gridHeight.toInt(),
            cells = cells
        )
    }

    private fun getCellPosition(row: Int, col: Int): Offset {
        return Offset(
            x = gridOffsetX + intervalSize + col * (cellSize + intervalSize) + cellSize / 2,
            y = gridOffsetY + intervalSize + row * (cellSize + intervalSize) + cellSize / 2
        )
    }

    fun getCellDrawPosition(row: Int, col: Int): Offset {
        return Offset(
            x = gridOffsetX + intervalSize + col * (cellSize + intervalSize),
            y = gridOffsetY + intervalSize + row * (cellSize + intervalSize)
        )
    }

    private val turrets = listOf(
        TurretEntry(Cannon(), weight = 1000),
        TurretEntry(Goliath(), weight = 1000),
        TurretEntry(Burst(), weight = 1000)
    )

    val playerTurretChoices = mutableStateListOf<TurretInterface>().apply {
        repeat(4) { add(getRandomTurret()) }
    }

    val enemyTurretChoices = mutableStateListOf<TurretInterface>().apply {
        repeat(4) { add(getRandomTurret()) }
    }

    val playerTurretsCooldown = mutableStateListOf<Long>().apply {
        repeat(4) { add(4000L) }
    }

    val enemyTurretsCooldown = mutableStateListOf<Long>().apply {
        repeat(4) { add(4000L) }
    }

    fun getTurret(turretType: TurretTypes): TurretInterface {
        for (entry in turrets) {
            if (entry.turret.turretType == turretType) {
                return entry.turret
            }
        }
        return turrets[0].turret
    }

    fun getRandomTurret(): TurretInterface {
        val totalWeight = turrets.sumOf { it.weight.toDouble() }.toFloat()
        val rand = Random.nextFloat() * totalWeight

        var cumulative = 0f
        for (entry in turrets) {
            cumulative += entry.weight
            if (rand <= cumulative) {
                return entry.turret
            }
        }

        return turrets.last().turret
    }
}

@Composable
@Preview
fun GridConquerorScreen(){

    val playerMana by remember { derivedStateOf { GameData.currentPlayerMana } }
    val enemyMana by remember { derivedStateOf { GameData.currentEnemyMana } }
    val enemyPercentage by remember { derivedStateOf { GameData.enemyPercentage } }
    val playerPercentage by remember { derivedStateOf { GameData.playerPercentage } }


    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()

        while (true) {
            val now = System.currentTimeMillis()
            val delta = now - lastTime
            lastTime = now

            for (i in GameData.playerTurretsCooldown.indices) {
                if (GameData.playerTurretsCooldown[i] > 0) {
                    GameData.playerTurretsCooldown[i] = maxOf(0, GameData.playerTurretsCooldown[i] - delta)
                }
            }

            for (i in GameData.enemyTurretsCooldown.indices) {
                if (GameData.enemyTurretsCooldown[i] > 0) {
                    GameData.enemyTurretsCooldown[i] = maxOf(0, GameData.enemyTurretsCooldown[i] - delta)
                }
            }

            delay(16L)
        }
    }

    Column{
        Box(modifier = Modifier
            .weight(.2f)
            .background(Color(0xFFA79095))
            .rotate(180f)
        ){
            ManaBar(
                mana = enemyMana,
                team = GameData.enemyTeam,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
            )

            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ){
                GameData.enemyTurretChoices.forEachIndexed { index, turret ->
                    if(index == 2 && GameData.grid != null){
                        Text(
                            text = String.format("%.2f %%", enemyPercentage),
                            color = GameData.enemyTeam.color
                        )
                    }
                    TurretCard(
                        turret,
                        cooldown = GameData.enemyTurretsCooldown[index],
                        team = GameData.enemyTeam,
                        isSelected = index == GameData.enemySelectedIndex,
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .clickable(enabled = GameData.mode != GridModes.SINGLE_PLAYER) {
                                if (GameData.enemyTurretsCooldown[index] <= 0L) {
                                    GameData.enemySelectedIndex = index
                                    GameData.enemySelectedTurretType = turret.turretType
                                }
                            }
                    )
                }
            }
        }

        Box(modifier = Modifier
            .weight(.8f)
        ){
            GridConquerorCanvas(modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .pointerInput(Unit){
                    detectTapGestures { offset ->
                        val team = GameData.grid!!.getTeamOfClosestCell(offset)
                        tryPlaceTurretAt(offset, team)
                    }
                }
            )
        }

        Box(modifier = Modifier
            .weight(.2f)
            .background(Color(0xFF90A79F))
        ){
            ManaBar(
                mana = playerMana,
                team = GameData.playerTeam,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ){
                GameData.playerTurretChoices.forEachIndexed { index, turret ->
                    if(index == 2 && GameData.grid != null){
                        Text(
                            text = String.format("%.2f %%", playerPercentage),
                            color = GameData.playerTeam.color
                        )
                    }
                    TurretCard(
                        turret,
                        cooldown =GameData.playerTurretsCooldown[index],
                        team = GameData.playerTeam,
                        isSelected = index == GameData.playerSelectedIndex,
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .clickable {
                                if (GameData.playerTurretsCooldown[index] <= 0L) {
                                    GameData.playerSelectedIndex = index
                                    GameData.playerSelectedTurretType = turret.turretType

                                }
                            }
                    )
                }
            }
        }

        if(GameData.showStartScreen){
            GridStartScreen()
        }

        if(GameData.showPauseScreen)
            GridPauseScreen()
    }
}

@Composable
fun GridStartScreen(){
    val options = listOf("TWO", "SINGLE")
    var selectedIndex by remember { mutableStateOf(0) }
    val singleSelected by remember { derivedStateOf { options[selectedIndex] == "SINGLE" } }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(32.dp)
    ){

        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth(.85f)
            ) {
                options.forEachIndexed{index, label ->
                    SegmentedButton(
                        modifier = Modifier
                            .padding(0.dp)
                            .height(64.dp),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            selectedIndex = index
                            GameData.mode = GridModes.entries[index]
                        },
                        selected = index == selectedIndex,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically){
                                when(label){
                                    "SINGLE" -> {
                                        if (index != selectedIndex) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = "single icon")
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                        Text(stringResource(R.string.single_player))
                                    }

                                    "TWO" -> {
                                        if (index != selectedIndex) {
                                            Icon(imageVector = Icons.Default.People, contentDescription = "two icon")
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                        Text(stringResource(R.string.two_player))
                                    }
                                }
                            }
                        },
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if(singleSelected){
                var diffSliderPosition by remember { mutableFloatStateOf(0f) }
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(.85f)
                ){
                    Text(text = stringResource(R.string.bot_difficulty) + " : " + stringResource(GridDifficulty.entries[diffSliderPosition.toInt()].titleId),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Slider(
                        value = diffSliderPosition,
                        onValueChange = {
                            diffSliderPosition = it
                            GameData.difficulty = GridDifficulty.entries[it.toInt()]
                        },
                        valueRange = 0f..4f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                        )
                    )
                }
            }

            var sliderPosition by remember { mutableFloatStateOf(GameData.numberOfColumn.toFloat()) }
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ){
                Text(
                    text = stringResource(R.string.columns_number) + ": ${sliderPosition.toInt()}",
                )

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        GameData.numberOfColumn = it.toInt()
                    },
                    valueRange = 10f..100f,
                    steps = 90,
                    colors = SliderDefaults.colors(
                    )
                )
            }
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                GameData.showStartScreen = false
                GameData.gameStateFlow.value = GameState.RUNNING
            },
        ){
            Text(
                text = stringResource(R.string.start),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

@Composable
fun GridPauseScreen(){
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp)
        .background(MaterialTheme.colorScheme.onSecondaryContainer)
    ){
        Text(
            text = stringResource(R.string.paused),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    GameData.showPauseScreen = false
                    GameData.gameStateFlow.value = GameState.RUNNING
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
            ) {
                Text(
                    text = stringResource(R.string.resume),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    GameData.showPauseScreen = false
                    GameData.gameStateFlow.value = GameState.STOPPED
                    restartGame()
                    GameData.showStartScreen = true
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
            ) {
                Text(
                    text = stringResource(R.string.restart),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        var sliderPosition by remember { mutableFloatStateOf(GameData.numberOfColumn.toFloat()) }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ){
            Text(
                text = stringResource(R.string.columns_number) + ": ${sliderPosition.toInt()}",
                color = MaterialTheme.colorScheme.onSecondary,
            )

            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    GameData.numberOfColumn = it.toInt()
                },
                valueRange = 10f..100f,
                steps = 90,
                colors = SliderDefaults.colors(
                )
            )
        }
    }
}

@Composable
fun GridConquerorCanvas(
    modifier: Modifier = Modifier
){
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var frameRequest by remember { mutableIntStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    if (GameData.grid == null) {
        GameData.grid = GameData.generateNewGrid(canvasSize.width, canvasSize.height)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            if (GameData.gameStateFlow.value == GameState.RUNNING) {
                val nano = withFrameNanos { it }
                if (lastFrameTime != 0L) {
                    deltaTime = (nano - lastFrameTime) / 1_000_000_000f
                }
                lastFrameTime = nano
                frameRequest++
                gameLoop(boundaries = GameData.gridSize)
            } else {
                lastFrameTime = 0L
                deltaTime     = 0f
                delay(16)
            }
        }
    }

    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0 &&
            GameData.gameStateFlow.value == GameState.RUNNING) {
            GameData.grid = GameData.generateNewGrid(canvasSize.width, canvasSize.height)
        }
    }

    val intervalPx = with(LocalDensity.current) { GameData.intervalSize.dp.toPx() }

    if(GameData.debugMode){
        Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
            val grid = GameData.grid

            // Fond noir complet
            drawRect(
                color = Color.Black,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )

            // DEBUG 1: Limites du canvas en blanc
            drawRect(
                color = Color.White,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = Stroke(width = 2.dp.toPx())
            )

            if (grid != null) {
                // DEBUG 2: Limites de la grille en rouge
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(GameData.gridOffsetX, GameData.gridOffsetY),
                    size = Size(GameData.gridWidth, GameData.gridHeight),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Grille normale
                val gridCells = grid.cells
                for (i in gridCells.indices) {
                    for (j in gridCells[i].indices) {
                        val drawPosition = GameData.getCellDrawPosition(i, j)
                        drawRect(
                            color = gridCells[i][j].team.color,
                            topLeft = drawPosition,
                            size = Size(GameData.cellSize, GameData.cellSize)
                        )
                    }
                }

                // DEBUG 3: Infos en haut à gauche
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        color = android.graphics.Color.YELLOW
                        textSize = 30f
                        textAlign = Paint.Align.LEFT
                    }

                    drawText("Canvas: ${size.width.toInt()}x${size.height.toInt()}", 10f, 40f, paint)
                    drawText("Grid: ${GameData.gridWidth.toInt()}x${GameData.gridHeight.toInt()}", 10f, 80f, paint)
                    drawText("Offset: (${GameData.gridOffsetX.toInt()}, ${GameData.gridOffsetY.toInt()})", 10f, 120f, paint)
                    drawText("Bullets: ${GameData.allBullets.size}", 10f, 160f, paint)
                    drawText("Turrets: ${GameData.placedTurrets.size}", 10f, 200f, paint)
                }

                // DEBUG 4: TOUTES les balles avec cercles verts
                for ((index, bullet) in GameData.allBullets.withIndex()) {
                    // Balle normale
                    bullet.draw(this)

                    drawCircle(
                        color = Color.Green,
                        radius = bullet.radius + 8f,
                        center = bullet.position,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = android.graphics.Color.CYAN
                            textSize = 24f
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(
                            "B$index: (${bullet.position.x.toInt()}, ${bullet.position.y.toInt()})",
                            bullet.position.x,
                            bullet.position.y - 25,
                            paint
                        )
                    }
                }

                // DEBUG 5: Tourelles avec cercles bleus
                for ((index, turret) in GameData.placedTurrets.withIndex()) {
                    turret.draw(this)

                    drawCircle(
                        color = Color.Blue,
                        radius = 30f,
                        center = turret.position,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = android.graphics.Color.MAGENTA
                            textSize = 20f
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(
                            "T$index: (${turret.position.x.toInt()}, ${turret.position.y.toInt()})",
                            turret.position.x,
                            turret.position.y + 40,
                            paint
                        )
                    }
                }

                // DEBUG 6: Centre de la grille
                val centerX = GameData.gridOffsetX + GameData.gridWidth / 2
                val centerY = GameData.gridOffsetY + GameData.gridHeight / 2
                drawCircle(
                    color = Color.Yellow,
                    radius = 15f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }else{
        Canvas(modifier = modifier
            .onSizeChanged { canvasSize = it }){
            val frame = frameRequest

            val totalSpacing = (GameData.numberOfColumn + 1) * intervalPx
            val cellSizePx = (canvasSize.width - totalSpacing) / GameData.numberOfColumn

            drawRect(
                color = Color.Black,
                topLeft = Offset(GameData.gridOffsetX, GameData.gridOffsetY),
                size = Size(GameData.gridWidth, GameData.gridHeight)
            )

            val grid = GameData.grid
            if (grid != null) {
                val gridCells = grid.cells

                for (i in gridCells.indices) {
                    for (j in gridCells[i].indices) {
                        val drawPosition = GameData.getCellDrawPosition(i, j)

                        drawRect(
                            color = gridCells[i][j].team.color,
                            topLeft = drawPosition,
                            size = Size(GameData.cellSize, GameData.cellSize)
                        )
                    }
                }
            }

            for(bullet in GameData.allBullets) {
                bullet.draw(this)
            }

            for(turret in GameData.placedTurrets){
                turret.draw(this)
            }
        }
    }
}

@Composable
fun TurretCard(
    turret: TurretInterface,
    cooldown: Long,
    team: Team,
    isSelected: Boolean,
    modifier: Modifier
){
    Box(modifier = modifier.aspectRatio(1f)){
        Card(modifier = Modifier
            .fillMaxSize()
            .let{
                if(isSelected) it.border(2.dp, selectedColor, RoundedCornerShape(8.dp)) else it
            },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFBCD4CA)
            )
        ){
            Box(modifier = Modifier
                .fillMaxSize()
            ){
                if(turret.drawableId != null)
                    Image(
                        painter =  painterResource(turret.drawableId!!),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                else
                    Text(
                        text =  stringResource(turret.nameId),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp)
                            .fillMaxSize()
                    )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(24.dp)
                .zIndex(1f)
                .clip(CircleShape)
                .background(manaColor, CircleShape)
                .border(3.dp, darkManaColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = turret.getTurretCost().toString(),
                color = onManaColor,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(cooldown / GameData.turretsCooldown.toFloat())
            .align(Alignment.BottomCenter)
            .background(Color.Gray.copy(alpha = .33f), RoundedCornerShape(8.dp))
        ){
            Text(modifier = Modifier.align(Alignment.Center), text = "%.2f".format(cooldown / 1000f))
        }
    }
}

@Composable
fun ManaBar(
    mana: Float,
    team: Team,
    modifier: Modifier
){
    val isPlayerTeam = team == GameData.playerTeam
    val fillValue = mana / GameData.globalMaxMana
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
//        Icon(
//            painter = painterResource(R.drawable.mana_drop),
//            contentDescription = "Mana drop",
//            Modifier.requiredSize(32.dp),
//            tint = manaColor,
//        )

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxSize()
                .border(2.dp, Color.Black)
                .clickable {
                    GameData.addManaBarClick()
                }
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillValue)
                    .fillMaxSize()
                    .background(manaColor)
            ){
                Text(
                    text = "${mana.toInt()} / ${GameData.globalMaxMana.toInt()}",
                    color = onManaColor,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

fun gameLoop(boundaries: IntSize){
    calculateNewBulletPosition(boundaries)
    checkForCellsCollisions()
    checkForBulletCollisions()
    checkForTurretCollisions()
    checkForLifetime()

    if(GameData.currentPlayerMana < GameData.globalMaxMana)
        GameData.currentPlayerMana += GameData.playerManaGrowRate * deltaTime

    if(GameData.currentEnemyMana < GameData.globalMaxMana)
        GameData.currentEnemyMana += GameData.enemyManaGrowRate * deltaTime

    if(GameData.grid != null){
        val enemyTotalCells = GameData.grid!!.getTotalNumberOfCells()
        val enemyCells = GameData.grid!!.getTeamNumberOfCells(GameData.enemyTeam)
        GameData.enemyPercentage = enemyCells.toFloat() / enemyTotalCells.toFloat() * 100f
        val playerTotalCells = GameData.grid!!.getTotalNumberOfCells()
        val playerCells = GameData.grid!!.getTeamNumberOfCells(GameData.playerTeam)
        GameData.playerPercentage = playerCells.toFloat() / playerTotalCells.toFloat() * 100f
    }

    for(turret in GameData.placedTurrets){
        turret.actionLoop(deltaTime)
    }

    if(GameData.mode == GridModes.SINGLE_PLAYER) {
        if (GameData.lastBotActionTime + GameData.difficulty.botPlayTime + ((-(GameData.difficulty.botPlayTime * .25f).toInt()..(GameData.difficulty.botPlayTime * .25f).toInt()).random()) < System.currentTimeMillis()){
            botAction()
            GameData.lastBotActionTime = System.currentTimeMillis()
        }
    }
}

private fun calculateNewBulletPosition(boundaries: IntSize) {
    for (bullet in GameData.allBullets) {
        val nextBulletPosition = bullet.position + bullet.direction * bullet.speed * deltaTime
        val previousBulletPosition = bullet.position

        if (nextBulletPosition.x - bullet.radius < 0 || nextBulletPosition.x + bullet.radius > boundaries.width) {
            bullet.direction = Offset(-bullet.direction.x, bullet.direction.y)
            bullet.position = previousBulletPosition
        } else if (nextBulletPosition.y - bullet.radius < 0 || nextBulletPosition.y + bullet.radius > boundaries.height) {
            bullet.direction = Offset(bullet.direction.x, -bullet.direction.y)
            bullet.position = previousBulletPosition
        } else {
            bullet.position = nextBulletPosition
        }
    }
}

private fun checkForLifetime(){
    val bulletsSnapshot = GameData.allBullets.toList()
    for (bullet in bulletsSnapshot) {
        if (bullet.createTime + bullet.lifetime < System.currentTimeMillis())
            GameData.allBullets.remove(bullet)
    }
}

private fun checkForCellsCollisions() {
    val bulletsSnapshot = GameData.allBullets.toList()
    for (bullet in bulletsSnapshot) {
        val hits = GameData.grid!!.getHittedCellsAndFaces(bullet.position, bullet.radius)
        if (hits.isEmpty()) continue

        for ((cell, face) in hits) {
            if (cell.team == bullet.team) continue

            bullet.hitCount -= 1

            if (bullet.canBounceOfCell) {
                if (face == Face.NORTH || face == Face.SOUTH) {
                    bullet.direction = bullet.direction.copy(y = -bullet.direction.y)
                } else {
                    bullet.direction = bullet.direction.copy(x = -bullet.direction.x)
                }
            }

            GameData.grid!!.cellHit(bullet.team, cell)

            if (bullet.hitCount <= 0) {
                GameData.allBullets.remove(bullet)
                break
            }
        }
    }
}

fun checkForBulletCollisions(){
    val bulletsSnapshot = GameData.allBullets.toList()
    val otherBulletsSnapshot = GameData.allBullets.toList()
    for (bullet in bulletsSnapshot) {
        for (otherBullet in otherBulletsSnapshot) {
            if (bullet == otherBullet || bullet.team == otherBullet.team) continue
            if ((bullet.position - otherBullet.position).getDistance() < bullet.radius + otherBullet.radius){
                if(bullet.hitCount == otherBullet.hitCount){
                    GameData.allBullets.remove(bullet)
                    GameData.allBullets.remove(otherBullet)
                }else if(bullet.hitCount > otherBullet.hitCount){
                    bullet.hitCount -= otherBullet.hitCount
                    GameData.allBullets.remove(otherBullet)
                    if(bullet.canBounceOfOtherBullets) bullet.direction.copy(y = -bullet.direction.y)
                }else{
                    otherBullet.hitCount -= bullet.hitCount
                    GameData.allBullets.remove(bullet)
                    if(otherBullet.canBounceOfOtherBullets) otherBullet.direction.copy(y = -otherBullet.direction.y)
                }
            }
        }
    }
}

fun checkForTurretCollisions(){
    val bulletsSnapshot = GameData.allBullets.toList()
    val turretSnapshot = GameData.placedTurrets.toList()
    for (bullet in bulletsSnapshot) {
        for (turret in turretSnapshot) {
            if (bullet.team == turret.team) continue
            if ((bullet.position - turret.position).getDistance() < bullet.radius + turret.baseSize / 2){
                turret.health -= bullet.damagePerHitPoint * bullet.hitCount
                if(turret.health <= 0) GameData.placedTurrets.remove(turret)
                GameData.allBullets.remove(bullet)
            }
        }
    }
}

private fun botAction(){
    var selectedTurret: TurretInterface? = null
    var attempt = 0
    val maxAttempts = 100
    while (selectedTurret == null && attempt < maxAttempts){
        attempt++
        val turret = GameData.enemyTurretChoices.random()
        if(turret.cost <= GameData.currentEnemyMana)
            selectedTurret = turret
    }
    if(selectedTurret == null) return

    GameData.enemySelectedIndex = GameData.enemyTurretChoices.indexOf(selectedTurret)
    GameData.enemySelectedTurretType = selectedTurret.turretType

    if(GameData.grid != null){
        if (GameData.grid!!.getTeamNumberOfCells(GameData.enemyTeam) <= 0){
            return
        }
        var position = GameData.grid!!.getRandomCellOfTeam(GameData.enemyTeam)!!.position
        attempt = 0

        while (!tryPlaceTurretAt(position, GameData.enemyTeam) && attempt < maxAttempts) {
            position = GameData.grid!!.getRandomCellOfTeam(GameData.enemyTeam)!!.position
            attempt++
        }
    }
}

private fun tryPlaceTurretAt(
    offset: Offset,
    team: Team
): Boolean {
    val selectedTurretType = when (team) {
        GameData.playerTeam -> GameData.playerSelectedTurretType
        GameData.enemyTeam -> GameData.enemySelectedTurretType
        else -> null
    } ?: return false

    val selectedIndex = when (team) {
        GameData.playerTeam -> GameData.playerSelectedIndex
        GameData.enemyTeam -> GameData.enemySelectedIndex
        else -> -1
    }
    if (selectedIndex < 0) return false

    val selectedTurret:TurretInterface = GameData.getTurret(selectedTurretType)

    var canPlace = true
    for (turret in GameData.placedTurrets) {
        if ((turret.position - offset).getDistance() < selectedTurret.baseSize + turret.baseSize / 2f &&
            GameData.grid!!.getTeamOfClosestCell(offset) == team
        ) {
            canPlace = false
            break
        }
    }

    val currentMana = when (team) {
        GameData.playerTeam -> GameData.currentPlayerMana
        GameData.enemyTeam -> GameData.currentEnemyMana
        else -> 0
    }

    if (currentMana.toInt() >= selectedTurret.cost && canPlace) {
        createTurret(selectedTurretType, offset, team)

        when (team) {
            GameData.playerTeam -> {
                GameData.currentPlayerMana -= selectedTurret.cost
                GameData.playerSelectedTurretType = null
                GameData.playerTurretsCooldown[selectedIndex] = GameData.turretsCooldown
                GameData.playerTurretChoices[selectedIndex] = GameData.getRandomTurret()
                GameData.playerSelectedIndex = -1
                return true
            }
            GameData.enemyTeam -> {
                GameData.currentEnemyMana -= selectedTurret.cost
                GameData.enemySelectedTurretType = null
                GameData.enemyTurretsCooldown[selectedIndex] = GameData.turretsCooldown
                GameData.enemyTurretChoices[selectedIndex] = GameData.getRandomTurret()
                GameData.enemySelectedIndex = -1
                return true
            }
            else ->{}
        }
    }

    return false
}

fun restartGame(){
    with(GameData){
        allBullets.clear()
        placedTurrets.clear()
        currentPlayerMana = 2f
        currentEnemyMana = 2f
        playerSelectedIndex = -1
        playerSelectedTurretType = null
        enemySelectedIndex = -1
        enemySelectedTurretType = null
        showPauseScreen = false
    }
}

fun createTurret(turretType: TurretTypes, offset: Offset, team: Team){
    val turret: TurretInterface
    val isPlayerTeam = team == GameData.playerTeam
    when(turretType){
        TurretTypes.CANNON -> {
            turret = Cannon(
                position = offset,
                baseSize = GameData.cellSize.toInt() * 3,
                cooldown = 1450f,
                team = team,
                orientationAngle = if(isPlayerTeam) 0f else 180f,
                rotationSpeed = 30f,
                minRotation = if(isPlayerTeam) -51f else 130f,
                maxRotation = if(isPlayerTeam) 50f else 230f,
                actionCallback = { angle ->
                    val angleRadians = Math.toRadians(angle.toDouble())
                    val directionX = cos(angleRadians)
                    val directionY = sin(angleRadians)

                    createBullet(
                        strokeRadius = 2f,
                        position = offset,
                        direction = Offset(directionX.toFloat(),
                            directionY.toFloat()
                        ),
                        speed = 500f,
                        radius = GameData.cellSize.toInt() / 3,
                        hitCount = 1,
                        team = team,
                        damagePerHitPoint = 5,
                        canBounceOfCell = true,
                        canBounceOfOtherBullets = true,
                        lifetime = 15000L
                    )
                }
            )
        }

        TurretTypes.GOLIATH -> {
            turret = Goliath(
                position = offset,
                baseSize = GameData.cellSize.toInt() * 4,
                cooldown = 5000f,
                team = team,
                orientationAngle = if(isPlayerTeam) 0f else 180f,
                rotationSpeed = 10f,
                minRotation = if(isPlayerTeam) -51f else 130f,
                maxRotation = if(isPlayerTeam) 50f else 230f,
                actionCallback = { angle ->
                    val angleRadians = Math.toRadians(angle.toDouble())
                    val directionX = cos(angleRadians)
                    val directionY = sin(angleRadians)

                    createBullet(
                        strokeRadius = 5f,
                        position = offset,
                        direction = Offset(directionX.toFloat(),
                            directionY.toFloat()
                        ),
                        speed = 100f,
                        radius = GameData.cellSize.toInt(),
                        hitCount = 20,
                        team = team,
                        damagePerHitPoint = 20,
                        canBounceOfCell = false,
                        canBounceOfOtherBullets = false,
                        lifetime = 30000L
                    )
                }
            )
        }

        TurretTypes.BURST -> {
            turret = Burst(
                position = offset,
                baseSize = GameData.cellSize.toInt() * 3,
                cooldown = 4000f,
                team = team,
                orientationAngle = if(isPlayerTeam) 0f else 180f,
                rotationSpeed = 30f,
                minRotation = if(isPlayerTeam) -51f else 130f,
                maxRotation = if(isPlayerTeam) 50f else 230f,
                numberOfBulletsInBurst = 15,
                cooldownBetweenShots = 50f,
                actionCallback = { angle ->
                    val angleRadians = Math.toRadians(angle.toDouble())
                    val directionX = cos(angleRadians)
                    val directionY = sin(angleRadians)

                    createBullet(
                        strokeRadius = 2f,
                        position = offset,
                        direction = Offset(directionX.toFloat(),
                            directionY.toFloat()
                        ),
                        speed = 500f,
                        radius = GameData.cellSize.toInt() / 3,
                        hitCount = 1,
                        team = team,
                        damagePerHitPoint = 5,
                        canBounceOfCell = true,
                        canBounceOfOtherBullets = true,
                        lifetime = 15000L
                    )
                }
            )
        }
    }
    GameData.placedTurrets.add(turret)
}

fun createBullet(strokeRadius: Float ,position: Offset, direction: Offset, speed: Float, radius: Int, hitCount: Int, team: Team, damagePerHitPoint: Int, canBounceOfOtherBullets: Boolean, canBounceOfCell: Boolean, lifetime: Long){
    GameData.allBullets.add(Bullet(
        strokeRadius = strokeRadius,
        position = position,
        direction = direction,
        speed = speed,
        radius = radius,
        hitCount = hitCount,
        team = team,
        damagePerHitPoint = damagePerHitPoint,
        canBounceOfCell = canBounceOfCell,
        canBounceOfOtherBullets = canBounceOfOtherBullets,
        lifetime = lifetime
    ))
}
