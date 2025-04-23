package com.SavenkoProjects.srspu_nav.navigation

import android.util.Log
import com.SavenkoProjects.srspu_nav.data.Constants.STAIR
import com.SavenkoProjects.srspu_nav.data.Constants.STAIRCASE_FINDER
import com.SavenkoProjects.srspu_nav.data.Constants.STAIRS
import com.SavenkoProjects.srspu_nav.data.Floor
import kotlin.math.sqrt

class StaircaseFinder {
    fun findNearestStaircase(x: Int, y: Int, floor: Floor): String {
        val staircases = floor.hallways.filter { (key, _) ->
            key.contains(STAIRS, ignoreCase = true) ||
            key.contains(STAIR, ignoreCase = true)
        }

        Log.d(STAIRCASE_FINDER, "Найдены лестницы на этаже ${floor.id}: ${staircases.keys}")

        if (staircases.isEmpty()) {
            Log.e(STAIRCASE_FINDER, "Лестницы не найдены на этаже ${floor.id}")
            return if (floor.id > 1) {
                val floorMainNode = "H${floor.id}"
                Log.d(STAIRCASE_FINDER, "Используем основной узел этажа: $floorMainNode")
                floorMainNode
            } else {
                "H${floor.id}"
            }
        }

        var nearestStaircaseId = ""
        var minDistance = Double.MAX_VALUE

        for ((staircaseId, hallway) in staircases) {
            val staircasePoint = hallway.path[0]
            val dx = x - staircasePoint[0]
            val dy = y - staircasePoint[1]
            val distance = sqrt((dx * dx + dy * dy).toDouble())
            if (distance < minDistance) {
                minDistance = distance
                nearestStaircaseId = staircaseId
            }
            Log.d(STAIRCASE_FINDER, "Расстояние до лестницы $staircaseId: $distance")
        }

        Log.d(STAIRCASE_FINDER,"Выбрана ближайшая лестница: $nearestStaircaseId")
        return nearestStaircaseId
    }
} 