package com.example.toolsapp.model.classes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.toolsapp.R

enum class Difficulty(val value: Int, val labelKey: Int, val despawnTime: Long){
    NONE(-1, R.string.unknow, 0),
    BOT(0, R.string.bot, 2500),
    EASY(1, R.string.easy, 2500),
    MEDIUM(2, R.string.medium, 2000),
    HARD(3, R.string.hard, 2000),
    EXTREME(4, R.string.extreme, 1500),
    IMPOSSIBLE(5, R.string.impossible, 1500),
    DEMON(6, R.string.demon, 1000),
    GODLY(7, R.string.godly, 1000),
}

fun Difficulty.getSpreadFactor(inverted: Boolean = false): Float {
    val min = 0.2f
    val max = 1.0f
    val steps = Difficulty.entries.size - 2
    var index = this.ordinal
    if(inverted)
        index = steps - index

    return min + (index / steps.toFloat()) * (max - min)
}

class Game(
){
    fun getLevel(difficulty: Difficulty, screenSize: Size): Level {
        var circleGroupsSequence = mutableListOf<Pair<Float, CircleGroup>>()
        var level = Level(circleGroupsSequence)
        val circleRadius = 64f

        val numberOfGroups = difficulty.value * (10 + (-5..5).random())

        for (i in 1..numberOfGroups){
            val numberOfCircles = difficulty.value * (10 + (-5..5).random())
            val groupColor = Color((0..255).random(), (0..255).random(), (0..255).random())

            var circlesSequence = mutableListOf<Pair<Float, Circle>>()
            var circleGroup = CircleGroup(circlesSequence, groupColor)

            for (j in 1..numberOfCircles){
                val minX = 0 + (circleRadius / 2).toInt()
                val maxX = screenSize.width * difficulty.getSpreadFactor() - (circleRadius / 2)
                val minY = 0 + (circleRadius / 2).toInt()
                val maxY = screenSize.height * difficulty.getSpreadFactor() - (circleRadius / 2)
                val spawnInterval = 1000f * difficulty.getSpreadFactor(true)

                val circle = Circle(
                    position = Offset((minX..maxX.toInt()).random().toFloat(), (minY..maxY.toInt()).random().toFloat()),
                    radius = circleRadius,
                    createTimestamp = 0,
                    remainingTime = 0,
                    color = groupColor,
                    number = j
                )

                circlesSequence.add(Pair(spawnInterval, circle))
            }

            val groupSpawnInterval = 1000f * difficulty.getSpreadFactor(true)
            circleGroupsSequence.add(Pair(groupSpawnInterval, circleGroup))
        }


        return level
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
    val number: Int
)