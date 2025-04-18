package com.SavenkoProjects.srspu_nav.navigation

import android.util.Log
import com.SavenkoProjects.srspu_nav.data.Constants.ITERATIONS_LIMIT_EXCEEDED
import com.SavenkoProjects.srspu_nav.data.Constants.NOT_FOUND_START_OR_END_POINT
import com.SavenkoProjects.srspu_nav.data.Constants.PATH_FINDER
import com.SavenkoProjects.srspu_nav.data.Floor


class PathFinder {
    fun findPath(floor: Floor, start: String, target: String): List<String> {
        Log.d(PATH_FINDER, "Поиск пути на этаже ${floor.id} от $start до $target")
        
        val startExists = floor.connections.containsKey(start) ||
                floor.doors.containsKey(start) ||
                floor.hallways.containsKey(start) ||
                start == "startPosition"

        val targetExists = floor.connections.containsKey(target) ||
                floor.doors.containsKey(target) ||
                floor.hallways.containsKey(target)
                
        Log.d(PATH_FINDER, "Начальная точка '$start' существует: $startExists")
        Log.d(PATH_FINDER, "Конечная точка '$target' существует: $targetExists")

        if (!startExists && floor.id > 1) {
            val floorMainNode = "H${floor.id}"
            if (floor.connections.containsKey(floorMainNode) || floor.hallways.containsKey(floorMainNode)) {
                Log.d(PATH_FINDER, "Заменяем начальную точку '$start' на основной узел этажа '$floorMainNode'")
                return findPath(floor, floorMainNode, target)
            }
        }

        if (!startExists || !targetExists) {
            Log.e(PATH_FINDER, NOT_FOUND_START_OR_END_POINT + floor.id)
            return emptyList()
        }

        val queue: MutableList<List<String>> = mutableListOf(listOf(start))
        val visited: MutableSet<String> = mutableSetOf()
        var iterations = 0
        val maxIterations = 1000

        while (queue.isNotEmpty() && iterations < maxIterations) {
            iterations++
            val path = queue.removeAt(0)
            val node = path.last()
            
            if (node == target) {
                Log.d(PATH_FINDER, "Путь найден за $iterations итераций: $path")
                return path
            }
            
            if (node !in visited) {
                visited.add(node)
                val neighbors = floor.connections[node] ?: emptyList()
                Log.d(PATH_FINDER, "Узел: $node, соседи: $neighbors")
                
                for (neighbor in neighbors) {
                    if (neighbor !in visited) {
                        val newPath = path + neighbor
                        queue.add(newPath)
                    }
                }
            }
        }

        if (iterations >= maxIterations) {
            Log.e(PATH_FINDER, ITERATIONS_LIMIT_EXCEEDED )
        } else {
            Log.e(PATH_FINDER, "Путь не найден. Посещено ${visited.size} узлов")
        }
        
        return emptyList()
    }
} 