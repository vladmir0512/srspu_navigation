package com.SavenkoProjects.srspu_nav.navigation

import android.util.Log
import com.SavenkoProjects.srspu_nav.data.Floor

class StaircaseFinder {
    fun findNearestStaircase(x: Int, floor: Floor): String {
        // Находим все лестницы на этаже
        val staircases = floor.hallways.filter { (key, _) ->
            key.contains("stairs", ignoreCase = true) || 
            key.contains("stair", ignoreCase = true) ||
            key.contains("HSL", ignoreCase = true) ||
            key.contains("HSR", ignoreCase = true)
        }

        Log.d("MapActivity", "Найдены лестницы на этаже ${floor.id}: ${staircases.keys}")

        if (staircases.isEmpty()) {
            Log.e("MapActivity", "Лестницы не найдены на этаже ${floor.id}")
            return if (floor.id > 1) {
                val floorMainNode = "H${floor.id}"
                Log.d("MapActivity", "Используем основной узел этажа: $floorMainNode")
                floorMainNode
            } else {
                "H${floor.id}"
            }
        }

        // Находим ближайшую лестницу
        var nearestStaircaseId = ""
        var minDistance = Int.MAX_VALUE

        for ((staircaseId, hallway) in staircases) {
            val staircasePoint = hallway?.path?.get(0)
            if (staircasePoint != null) {
                val distance = kotlin.math.abs(x - staircasePoint[0])
                if (distance < minDistance) {
                    minDistance = distance
                    nearestStaircaseId = staircaseId
                }
                Log.d("MapActivity", "Расстояние до лестницы $staircaseId: $distance")
            }
        }

        Log.d("MapActivity", "Выбрана ближайшая лестница: $nearestStaircaseId")
        return nearestStaircaseId
    }
} 