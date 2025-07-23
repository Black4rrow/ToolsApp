package com.example.toolsapp.model.classes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.toolsapp.R

enum class Difficulty(val value: Int, val labelKey: Int, val despawnTime: Long, val iconId: Int){
    NONE(-1, R.string.unknow, 0, R.drawable.picture_icon),
    BOT(0, R.string.bot, 999999999, R.drawable.check),
    EASY(1, R.string.easy, 2500, R.drawable.check),
    MEDIUM(2, R.string.medium, 2000, R.drawable.check),
    HARD(3, R.string.hard, 2000, R.drawable.check),
    EXTREME(4, R.string.extreme, 1500, R.drawable.check),
    IMPOSSIBLE(5, R.string.impossible, 1500, R.drawable.check),
    DEMON(6, R.string.demon, 1000, R.drawable.check),
    GODLY(7, R.string.godly, 1000, R.drawable.check),
    INFINITE(-2, R.string.endless, 2000, R.drawable.infinite);

    companion object{
        val secondsToNextDifficulty = 60
        fun difficultyFromPlayTime(playTime: Long): Difficulty{
            var difficultyValue = (playTime / (secondsToNextDifficulty*1000)).toInt()
            difficultyValue = difficultyValue.coerceIn(1, entries.maxOf { it.value })
            return entries.firstOrNull{it.value == difficultyValue} ?: EASY
        }
    }
}

fun Difficulty.getSpreadFactor(inverted: Boolean = false): Float {
    val min = 0.2f
    val max = 1.0f
    val steps = Difficulty.entries.size - 3
    val rawIndex = this.ordinal
    val index = if (inverted) (steps - rawIndex).coerceIn(0, steps)
    else rawIndex.coerceIn(0, steps)

    return min + (index / steps.toFloat()) * (max - min)
}

class Game(
){
    fun getLevel(difficulty: Difficulty, screenSize: Size, backgroundColor: Color): Level {
        var circleGroupsSequence = mutableListOf<Pair<Float, CircleGroup>>()
        var level = Level(circleGroupsSequence)
        val circleRadius = 64f
        val padding = circleRadius

        val numberOfGroups = 15 + (-5..5).random()

        for (i in 1..numberOfGroups){
            val numberOfCircles = difficulty.value * (10 + (-5..5).random())
            var groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
            while(ColorUtils.calculateContrast(groupColor.toArgb(), backgroundColor.toArgb()) < 1.5f){
                groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
            }

            var circlesSequence = mutableListOf<Pair<Float, Circle>>()
            var circleGroup = CircleGroup(circlesSequence, groupColor)

            for (j in 1..numberOfCircles){
                val spawnWidth  = screenSize.width  * 1/*difficulty.getSpreadFactor()*/
                val spawnHeight = screenSize.height * 1/*difficulty.getSpreadFactor()*/

                val minX = padding + circleRadius
                val maxX = spawnWidth  - padding - circleRadius
                val minY = padding + circleRadius
                val maxY = spawnHeight - padding - circleRadius

                val spawnInterval = 1000f * difficulty.getSpreadFactor(true)

                val circle = Circle(
                    position = Offset((minX.toInt()..maxX.toInt()).random().toFloat(), (minY.toInt()..maxY.toInt()).random().toFloat()),
                    radius = circleRadius,
                    createTimestamp = 0,
                    remainingTime = 0,
                    color = groupColor,
                    number = j
                )

                circlesSequence.add(Pair(spawnInterval, circle))
            }

            val groupSpawnInterval = 2000f * difficulty.getSpreadFactor(true)
            circleGroupsSequence.add(Pair(groupSpawnInterval, circleGroup))
        }


        return level
    }

    fun getGroup(playTime: Long, screenSize: Size, backgroundColor: Color): Pair<Float, CircleGroup> {
        val difficulty:Difficulty = Difficulty.difficultyFromPlayTime(playTime)

        val circleRadius = 64f
        val padding = circleRadius
        val numberOfCircles = difficulty.value * (10 + (-5..5).random())
        var groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
        while(ColorUtils.calculateContrast(groupColor.toArgb(), backgroundColor.toArgb()) < 1.5f){
            groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())
        }

        var circlesSequence = mutableListOf<Pair<Float, Circle>>()
        var circleGroup = CircleGroup(circlesSequence, groupColor)

        val groupSpawnInterval = 2000f * difficulty.getSpreadFactor(true)

        for (j in 1..numberOfCircles){
            val spawnWidth  = screenSize.width  * 1/*difficulty.getSpreadFactor()*/
            val spawnHeight = screenSize.height * 1/*difficulty.getSpreadFactor()*/

            val minX = padding + circleRadius
            val maxX = spawnWidth  - padding - circleRadius
            val minY = padding + circleRadius
            val maxY = spawnHeight - padding - circleRadius

            val spawnInterval = 1000f * difficulty.getSpreadFactor(true)

            val circle = Circle(
                position = Offset((minX.toInt()..maxX.toInt()).random().toFloat(), (minY.toInt()..maxY.toInt()).random().toFloat()),
                radius = circleRadius,
                createTimestamp = 0,
                remainingTime = 0,
                color = groupColor,
                number = j
            )

            circlesSequence.add(Pair(spawnInterval, circle))
        }

        return Pair(groupSpawnInterval, circleGroup)
    }
}

data class Level(
    var circleGroupsSequence: MutableList<Pair<Float, CircleGroup>>,
){
    fun popNextCircleGroup(): Pair<Float, CircleGroup>? {
        if(circleGroupsSequence.isEmpty())
            return null

        return circleGroupsSequence.removeAt(0)
    }
}

data class CircleGroup(
    var circlesSequence: MutableList<Pair<Float, Circle>>,
    val color: Color
){
    fun popNextCircle(): Pair<Float, Circle>? {
        if(circlesSequence.isEmpty())
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
    var feedbackText: String? = null,
    var feedbackTimestamp: Long = 0L
)