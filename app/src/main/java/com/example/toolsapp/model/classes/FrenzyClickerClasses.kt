package com.example.toolsapp.model.classes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.toolsapp.R
import kotlin.math.sqrt

enum class Difficulty(
    val value: Int,
    val labelKey: Int,
    val despawnTime: Long,
    val iconId: Int,
    val baseNumberOfGroups: Int,
    val baseNumberOfCirclesPerGroup: Int,
    val baseCircleSpawnInterval: Long,
) {
    NONE(
        value = -1,
        labelKey = R.string.unknow,
        despawnTime = 0,
        iconId = R.drawable.picture_icon,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 10,
        baseCircleSpawnInterval = 1000
    ),

    BOT(
        value = 0,
        labelKey = R.string.bot,
        despawnTime = 1500,
        iconId = R.drawable.computer,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 10,
        baseCircleSpawnInterval = 1000
    ),

    EASY(
        value = 1,
        labelKey = R.string.easy,
        despawnTime = 2500,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 10,
        baseCircleSpawnInterval = 1000
    ),

    MEDIUM(
        value = 2,
        labelKey = R.string.normal,
        despawnTime = 2000,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 10,
        baseCircleSpawnInterval = 1000
    ),

    HARD(
        value = 3,
        labelKey = R.string.hard,
        despawnTime = 2000,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 15,
        baseCircleSpawnInterval = 750
    ),

    EXTREME(
        value = 4,
        labelKey = R.string.extreme,
        despawnTime = 1500,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 15,
        baseCircleSpawnInterval = 500
    ),

    IMPOSSIBLE(value = 5,
        labelKey = R.string.impossible,
        despawnTime = 1500,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 15,
        baseCircleSpawnInterval = 350
    ),

    DEMON(value = 6,
        labelKey = R.string.demon,
        despawnTime = 1000,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 20,
        baseCircleSpawnInterval = 250
    ),

    GODLY(value = 7,
        labelKey = R.string.godly,
        despawnTime = 1000,
        iconId = R.drawable.check,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 20,
        baseCircleSpawnInterval = 150
    ),

    UNKNOWN(
        value = 8,
        labelKey = R.string.unknown_diff,
        despawnTime = 1000,
        iconId = R.drawable.unknown,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 200,
        baseCircleSpawnInterval = 50
    ),

    INFINITE(value = -2,
        labelKey = R.string.endless,
        despawnTime = 2000,
        iconId = R.drawable.infinite,
        baseNumberOfGroups = 999,
        baseNumberOfCirclesPerGroup = 10,
        baseCircleSpawnInterval = 1000
    );

    companion object {
        private const val SECONDS_TO_NEXT_DIFFICULTY = 60
        fun difficultyFromPlayTime(playTime: Long): Difficulty {
            var difficultyValue = (playTime / (SECONDS_TO_NEXT_DIFFICULTY * 1000)).toInt()
            difficultyValue = difficultyValue.coerceIn(1, entries.maxOf { it.value })
            return entries.firstOrNull { it.value == difficultyValue } ?: EASY
        }
    }
}


class Game(
    var order:Int = 0
) {
    fun getLevel(difficulty: Difficulty, screenSize: Size, backgroundColor: Color): Level {
        val circleGroupsSequence = mutableListOf<Pair<Long, CircleGroup>>()
        val level = Level(circleGroupsSequence)
        val circleRadius = 64f
        val padding = circleRadius

        val numberOfGroups = difficulty.baseNumberOfGroups + (-5..5).random()

        var order = 0

        for (i in 1..numberOfGroups) {
            val numberOfCircles = difficulty.baseNumberOfCirclesPerGroup + (-5..5).random()

            var groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
            while (ColorUtils.calculateContrast(
                    groupColor.toArgb(),
                    backgroundColor.toArgb()
                ) < 1.5f
            ) {
                groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
            }

            var circlesSequence = mutableListOf<Pair<Long, Circle>>()
            var circleGroup = CircleGroup(circlesSequence, groupColor)

            var lastCircle: Circle? = null
            for (j in 1..numberOfCircles) {
                val spawnWidth = screenSize.width * 1/*difficulty.getSpreadFactor()*/
                val spawnHeight = screenSize.height * 1/*difficulty.getSpreadFactor()*/

                val minX = padding + circleRadius
                val maxX = spawnWidth - padding - circleRadius
                val minY = padding + circleRadius
                val maxY = spawnHeight - padding - circleRadius

                val spawnInterval = difficulty.baseCircleSpawnInterval

                var position = Offset(
                    (minX.toInt()..maxX.toInt()).random().toFloat(),
                    (minY.toInt()..maxY.toInt()).random().toFloat())

                if (lastCircle != null){
                    while(distance(position, lastCircle.position) < 2 * circleRadius){
                        position = Offset(
                            (minX.toInt()..maxX.toInt()).random().toFloat(),
                            (minY.toInt()..maxY.toInt()).random().toFloat())
                    }
                }

                val circle = Circle(
                    position = position,
                    radius = circleRadius,
                    createTimestamp = 0,
                    remainingTime = 0,
                    color = groupColor,
                    number = j,
                    order = order++
                )

                circlesSequence.add(Pair(spawnInterval, circle))
                lastCircle = circle
            }

            val groupSpawnInterval = 2000L
            circleGroupsSequence.add(Pair(groupSpawnInterval, circleGroup))
        }


        return level
    }

    fun getGroup(
        playTime: Long,
        screenSize: Size,
        backgroundColor: Color
    ): Pair<Long, CircleGroup> {
        val difficulty: Difficulty = Difficulty.difficultyFromPlayTime(playTime)
        val circleRadius = 64f
        val padding = circleRadius / 1

        val numberOfCircles = difficulty.baseNumberOfCirclesPerGroup + (playTime/10000f).toInt() + (-5..5).random()

        var groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
        while (ColorUtils.calculateContrast(groupColor.toArgb(), backgroundColor.toArgb()) < 1.5f) {
            groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
        }

        var circlesSequence = mutableListOf<Pair<Long, Circle>>()
        var circleGroup = CircleGroup(circlesSequence, groupColor)

        val groupSpawnInterval = 2000L

        var lastCircle: Circle? = null
        for (j in 1..numberOfCircles) {
            val spawnWidth = screenSize.width * 1/*difficulty.getSpreadFactor()*/
            val spawnHeight = screenSize.height * 1/*difficulty.getSpreadFactor()*/

            val minX = padding + circleRadius
            val maxX = spawnWidth - padding - circleRadius
            val minY = padding + circleRadius
            val maxY = spawnHeight - padding - circleRadius

            val spawnInterval = difficulty.baseCircleSpawnInterval

            var position = Offset(
                (minX.toInt()..maxX.toInt()).random().toFloat(),
                (minY.toInt()..maxY.toInt()).random().toFloat())

            if (lastCircle != null){
                while(distance(position, lastCircle.position) < 2 * circleRadius){
                    position = Offset(
                        (minX.toInt()..maxX.toInt()).random().toFloat(),
                        (minY.toInt()..maxY.toInt()).random().toFloat())
                }
            }

            val circle = Circle(
                position = position,
                radius = circleRadius,
                createTimestamp = 0,
                remainingTime = 0,
                color = groupColor,
                number = j,
                order = this.order++
            )

            circlesSequence.add(Pair(spawnInterval, circle))
            lastCircle = circle
        }

        return Pair(groupSpawnInterval, circleGroup)
    }

    fun distance(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}

data class Level(
    var circleGroupsSequence: MutableList<Pair<Long, CircleGroup>>,
) {
    fun popNextCircleGroup(): Pair<Long, CircleGroup>? {
        if (circleGroupsSequence.isEmpty())
            return null

        return circleGroupsSequence.removeAt(0)
    }
}

data class CircleGroup(
    var circlesSequence: MutableList<Pair<Long, Circle>>,
    val color: Color
) {
    fun popNextCircle(): Pair<Long, Circle>? {
        if (circlesSequence.isEmpty())
            return null

        return circlesSequence.removeAt(0)
    }
}

data class Circle(
    val color: Color,
    val position: Offset,
    val radius: Float,
    var createTimestamp: Long,
    var remainingTime: Long,
    val number: Int,
    var order: Int,
    var feedbackText: String? = null,
    var feedbackTimestamp: Long = 0L,
)