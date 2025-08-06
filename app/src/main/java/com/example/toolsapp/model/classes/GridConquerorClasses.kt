package com.example.toolsapp.model.classes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.toolsapp.R
import com.example.toolsapp.ui.screens.Face
import com.example.toolsapp.ui.screens.GameData
import com.example.toolsapp.ui.screens.Team
import com.example.toolsapp.ui.screens.TurretTypes
import com.example.toolsapp.ui.theme.blueTeamTurretColor
import com.example.toolsapp.ui.theme.healthColor
import com.example.toolsapp.ui.theme.nohealthColor
import com.example.toolsapp.ui.theme.redTeamTurretColor
import com.example.toolsapp.ui.theme.turretCannonColor
import kotlin.math.abs
import kotlin.math.sqrt

interface TurretInterface{
    var position: Offset
    var nameId: Int
    var drawableId: Int?
    var cost: Int
    var turretType: TurretTypes
    var baseSize: Int
    var maxHealth: Int
    var health: Int
    val team: Team

    fun draw(drawScope: DrawScope){
        with(drawScope){
            val turretCenter = Offset(
                x = position.x,
                y = position.y
            )
            val topLeft = turretCenter - Offset(
                x = baseSize / 2f,
                y = -baseSize / 2f
            )

            drawRect(
                color = nohealthColor,
                topLeft = topLeft,
                size = Size(baseSize.toFloat(), baseSize.toFloat() / 4f)
            )
            drawRect(
                color = healthColor,
                topLeft = topLeft,
                size = Size(baseSize * (health/maxHealth.toFloat()), baseSize.toFloat() / 4f)
            )
        }
    }
    fun actionLoop(detlaTime: Float)
    fun getTurretIcon(): Int?{return drawableId}
    fun getTurretName(): Int{return nameId}
    fun getTurretCost(): Int{return cost}
    fun getTurretHealth(): Int{return health}
    fun getCooldownRandomized(cooldown: Float): Float{
        val percentageModifier = .2f
        return cooldown + ((-cooldown*percentageModifier).toInt()..(cooldown*percentageModifier).toInt()).random()
    }
}

data class Cannon(
    override var position: Offset = Offset.Zero,
    override var nameId: Int = R.string.cannon,
    override var drawableId: Int? = null,
    override var cost: Int = 4,
    override var turretType: TurretTypes = TurretTypes.CANNON,
    override var baseSize: Int = 0,
    override var maxHealth: Int = 100,
    override var health: Int = 100,
    override val team: Team = Team.BLUE,
    var lastActionTime: Long = 0L,
    val actionCallback: (angle: Float) -> Unit = {},
    var orientationAngle: Float = 0f,
    val rotationSpeed: Float = 360f,
    val minRotation: Float = 0f,
    val maxRotation: Float = 0f,
    val cooldown: Float = 999f,

    var rotationDirection: Int = 1,
) : TurretInterface {
    override fun draw(drawScope: DrawScope) {
        val baseColor = if(team == Team.RED) redTeamTurretColor else blueTeamTurretColor
        val turretCenter = Offset(
            x = position.x,
            y = position.y
        )
        val cannonWidth = baseSize / 3

        with(drawScope){
            drawRect(
                color = Color.Black,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2 - 4,
                    turretCenter.y - baseSize / 2 - 4),
                size = Size(baseSize.toFloat() + 8, baseSize.toFloat() + 8)

            )
            drawRect(
                color = baseColor,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2,
                    turretCenter.y - baseSize / 2),
                size = Size(baseSize.toFloat(), baseSize.toFloat())

            )

            rotate(
                degrees = orientationAngle + 180,
                pivot = turretCenter
            ){
                drawRect(
                    color = turretCannonColor,
                    topLeft = Offset(
                        x = turretCenter.x - cannonWidth / 2,
                        y = turretCenter.y
                    ),
                    size = Size(cannonWidth.toFloat(), baseSize.toFloat() / 2)
                )

                drawCircle(
                    color = turretCannonColor,
                    center = turretCenter,
                    radius = baseSize / 3f
                )
            }
        }
        super<TurretInterface>.draw(drawScope)
    }

    override fun actionLoop(detlaTime: Float) {
        orientationAngle += rotationSpeed * rotationDirection * detlaTime
        if(orientationAngle >= maxRotation) rotationDirection = -1
        else if(orientationAngle <= minRotation) rotationDirection = 1


        val currentTime = System.currentTimeMillis()
        if(currentTime - lastActionTime > getCooldownRandomized(cooldown)){
            lastActionTime = currentTime
            actionCallback(orientationAngle - 90)
        }
    }
}

data class Goliath(
    override var position: Offset = Offset.Zero,
    override var nameId: Int = R.string.goliath,
    override var drawableId: Int? = null,
    override var cost: Int = 8,
    override var turretType: TurretTypes = TurretTypes.GOLIATH,
    override var baseSize: Int = 0,
    override var maxHealth: Int = 500,
    override var health: Int = 500,
    override val team: Team = Team.BLUE,
    var lastActionTime: Long = 0L,
    val actionCallback: (angle: Float) -> Unit = {},
    var orientationAngle: Float = 0f,
    val rotationSpeed: Float = 360f,
    val minRotation: Float = 0f,
    val maxRotation: Float = 0f,
    val cooldown: Float = 999f,

    var rotationDirection: Int = 1,
) : TurretInterface {
    override fun draw(drawScope: DrawScope) {
        val baseColor = if(team == Team.RED) redTeamTurretColor else blueTeamTurretColor
        val turretCenter = Offset(
            x = position.x,
            y = position.y
        )
        val cannonWidth = baseSize / 1.5f

        with(drawScope){
            drawRect(
                color = Color.Black,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2 - 4,
                    turretCenter.y - baseSize / 2 - 4),
                size = Size(baseSize.toFloat() + 8, baseSize.toFloat() + 8)

            )
            drawRect(
                color = baseColor,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2,
                    turretCenter.y - baseSize / 2),
                size = Size(baseSize.toFloat(), baseSize.toFloat())

            )

            rotate(
                degrees = orientationAngle + 180,
                pivot = turretCenter
            ){
                drawRect(
                    color = turretCannonColor,
                    topLeft = Offset(
                        x = turretCenter.x - cannonWidth / 2,
                        y = turretCenter.y
                    ),
                    size = Size(cannonWidth.toFloat(), baseSize.toFloat() / 2)
                )

                drawCircle(
                    color = turretCannonColor,
                    center = turretCenter,
                    radius = baseSize / 3f
                )
            }
        }
        super<TurretInterface>.draw(drawScope)
    }

    override fun actionLoop(detlaTime: Float) {
        orientationAngle += rotationSpeed * rotationDirection * detlaTime
        if(orientationAngle >= maxRotation) rotationDirection = -1
        else if(orientationAngle <= minRotation) rotationDirection = 1


        val currentTime = System.currentTimeMillis()
        if(currentTime - lastActionTime > getCooldownRandomized(cooldown)){
            lastActionTime = currentTime
            actionCallback(orientationAngle - 90)
        }
    }
}

data class Burst(
    override var position: Offset = Offset.Zero,
    override var nameId: Int = R.string.burst,
    override var drawableId: Int? = null,
    override var cost: Int = 6,
    override var turretType: TurretTypes = TurretTypes.BURST,
    override var baseSize: Int = 0,
    override var maxHealth: Int = 200,
    override var health: Int = 200,
    override val team: Team = Team.BLUE,
    var lastActionTime: Long = 0L,
    val actionCallback: (angle: Float) -> Unit = {},
    var orientationAngle: Float = 0f,
    val rotationSpeed: Float = 360f,
    val minRotation: Float = 0f,
    val maxRotation: Float = 0f,
    val cooldown: Float = 999f,
    val numberOfBulletsInBurst: Int = 10,
    val cooldownBetweenShots: Float = 50f,

    var rotationDirection: Int = 1,
    var burstStartedAt: Long = 0L,
    var lastBulletShotAt: Long = 0L,
    var bulletsFiredInThisBurst: Int = 0
) : TurretInterface {
    override fun draw(drawScope: DrawScope) {
        val baseColor = if(team == Team.RED) redTeamTurretColor else blueTeamTurretColor
        val turretCenter = Offset(
            x = position.x,
            y = position.y
        )
        val cannonWidth = baseSize / 3f

        with(drawScope){
            drawRect(
                color = Color.Black,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2 - 4,
                    turretCenter.y - baseSize / 2 - 4),
                size = Size(baseSize.toFloat() + 8, baseSize.toFloat() + 8)

            )
            drawRect(
                color = baseColor,
                topLeft = Offset(
                    turretCenter.x - baseSize / 2,
                    turretCenter.y - baseSize / 2),
                size = Size(baseSize.toFloat(), baseSize.toFloat())

            )

            rotate(
                degrees = orientationAngle + 180,
                pivot = turretCenter
            ){
                drawRect(
                    color = turretCannonColor,
                    topLeft = Offset(
                        x = turretCenter.x - cannonWidth / 2,
                        y = turretCenter.y
                    ),
                    size = Size(cannonWidth.toFloat(), baseSize.toFloat())
                )

                drawCircle(
                    color = turretCannonColor,
                    center = turretCenter,
                    radius = baseSize / 3f
                )
            }
        }
        super<TurretInterface>.draw(drawScope)
    }

    override fun actionLoop(detlaTime: Float) {
        orientationAngle += rotationSpeed * rotationDirection * detlaTime
        if(orientationAngle >= maxRotation) rotationDirection = -1
        else if(orientationAngle <= minRotation) rotationDirection = 1


        val now = System.currentTimeMillis()

        if (bulletsFiredInThisBurst == 0) {
            if (now - lastActionTime >= getCooldownRandomized(cooldown)) {
                burstStartedAt = now
                lastBulletShotAt = now
                bulletsFiredInThisBurst = 1
                actionCallback(orientationAngle - 90)
            }
            return
        }

        if (bulletsFiredInThisBurst in 1 until numberOfBulletsInBurst) {
            if (now - lastBulletShotAt >= cooldownBetweenShots) {
                bulletsFiredInThisBurst++
                lastBulletShotAt = now
                actionCallback(orientationAngle - 90)
            }
            return
        }

        if (bulletsFiredInThisBurst >= numberOfBulletsInBurst) {
            bulletsFiredInThisBurst = 0
            lastActionTime = now
        }
    }
}

class Grid(
    val width: Int,
    val height: Int,
    val cells: Array<Array<Cell>>
){
    fun cellHit(team: Team, cell: Cell){
        for(i in cells.indices){
            for(j in cells[i].indices){
                if(cells[i][j] == cell) cells[i][j].team = team
            }
        }
    }

    fun getHittedCellsAndFaces(position: Offset, radius: Int): List<Pair<Cell, Face>> {
        val intervalSize  = GameData.intervalSize
        val totalCellSize = GameData.cellSize + intervalSize

        val approxCol = ((position.x - GameData.gridOffsetX - intervalSize) / totalCellSize).toInt()
        val approxRow = ((position.y - GameData.gridOffsetY - intervalSize) / totalCellSize).toInt()

        val hits = mutableListOf<Pair<Cell, Face>>()

        for (i in (approxRow - 1)..(approxRow + 1)) {
            for (j in (approxCol - 1)..(approxCol + 1)) {
                val cell = cells.getOrNull(i)?.getOrNull(j) ?: continue
                val dx = position.x - cell.position.x
                val dy = position.y - cell.position.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance <= (cell.radius + radius)) {
                    val face = when {
                        abs(dx) > abs(dy) && dx > 0 -> Face.WEST
                        abs(dx) > abs(dy) && dx < 0 -> Face.EAST
                        abs(dy) > abs(dx) && dy > 0 -> Face.NORTH
                        abs(dy) > abs(dx) && dy < 0 -> Face.SOUTH
                        else                       -> Face.NORTH
                    }
                    hits += cell to face
                }
            }
        }
        return hits
    }

    fun getTeamOfClosestCell(position: Offset): Team {
        val cellDiameter = cells[0][0].radius * 2
        val intervalSize = GameData.intervalSize
        val totalCellSize = cellDiameter + intervalSize

        val approxCol = ((position.x - intervalSize) / totalCellSize).toInt()
        val approxRow = ((position.y - intervalSize) / totalCellSize).toInt()

        var closestCell: Cell? = null
        var minDistance = Float.MAX_VALUE

        for (i in (approxRow - 1)..(approxRow + 1)) {
            for (j in (approxCol - 1)..(approxCol + 1)) {
                val cell = cells.getOrNull(i)?.getOrNull(j) ?: continue
                val dist = (cell.position - position).getDistance()
                if (dist < minDistance) {
                    minDistance = dist
                    closestCell = cell
                }
            }
        }

        return closestCell?.team ?: Team.RED
    }

    fun getRandomCellOfTeam(team: Team): Cell?{
        val teamCells = cells.flatten().filter { it.team == team }
        if(teamCells.isEmpty()) return null
        return teamCells.random()
    }

    fun getTotalNumberOfCells(): Int{
        return cells.flatten().size
    }

    fun getTeamNumberOfCells(team: Team):Int{
        return cells.flatten().count { it.team == team }
    }
}

class Cell(
    var team: Team,
    val position: Offset,
    val radius: Int
)

class Bullet(
    var radius: Int = 40,
    var strokeRadius: Float = 5f,
    var hitCount: Int = 1,
    var speed:Float = 2.5f,
    var position: Offset = Offset.Zero,
    var direction: Offset = Offset.Zero,
    var team: Team = Team.BLUE,
    var canBounceOfCell: Boolean = true,
    var canBounceOfOtherBullets: Boolean = true,
    var damagePerHitPoint: Int = 5,
    var createTime: Long = System.currentTimeMillis(),
    val lifetime: Long = 15000L,
){
    fun draw(drawScope: DrawScope){
        with(drawScope){
            val color = when(team){
                Team.RED -> Color(0xFFF6C900)
                Team.BLUE -> Color.Cyan
            }

            drawCircle(
                color = color,
                radius = radius.toFloat(),
                center = position
            )

            drawCircle(
                color = Color.Black,
                radius = radius.toFloat(),
                center = position,
                style = Stroke(width = strokeRadius)
            )
        }
    }
}