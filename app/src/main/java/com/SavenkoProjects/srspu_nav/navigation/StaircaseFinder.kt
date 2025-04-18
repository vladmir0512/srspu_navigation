package com.SavenkoProjects.srspu_nav.navigation

import android.util.Log
import com.SavenkoProjects.srspu_nav.data.Constants.HSL
import com.SavenkoProjects.srspu_nav.data.Constants.HSR
import com.SavenkoProjects.srspu_nav.data.Constants.STAIR
import com.SavenkoProjects.srspu_nav.data.Constants.STAIRCASE_FINDER
import com.SavenkoProjects.srspu_nav.data.Constants.STAIRS
import com.SavenkoProjects.srspu_nav.data.Floor

class StaircaseFinder {
    fun findNearestStaircase(x: Int, floor: Floor): String {
        // Находим все лестницы на этаже
        val staircases = floor.hallways.filter { (key, _) ->
            key.contains(STAIRS, ignoreCase = true) ||
            key.contains(STAIR, ignoreCase = true) ||
            key.contains(HSL, ignoreCase = true) ||
            key.contains(HSR, ignoreCase = true)
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

        // Находим ближайшую лестницу
        var nearestStaircaseId = ""
        var minDistance = Int.MAX_VALUE

        for ((staircaseId, hallway) in staircases) {
            val staircasePoint = hallway.path.get(0)
            val distance = kotlin.math.abs(x - staircasePoint[0])
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