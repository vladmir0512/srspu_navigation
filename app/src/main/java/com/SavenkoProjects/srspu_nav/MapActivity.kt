package com.SavenkoProjects.srspu_nav

import android.graphics.*
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import java.io.IOException

class MapActivity : AppCompatActivity() {
    private var building: Building? = null
    private lateinit var mapImageView: ImageView
    private lateinit var floorMapImageView: ImageView
    private lateinit var roomNumberEditText: EditText
    private lateinit var findButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        roomNumberEditText = findViewById(R.id.roomNumberEditText)
        findButton = findViewById(R.id.findButton)
        mapImageView = findViewById(R.id.mapImageView)
        floorMapImageView = findViewById(R.id.floorMapImageView)

        val json = intent.getStringExtra("buildingJson")
        if (json != null) {
            building = Gson().fromJson(json, Building::class.java)
            Log.d("MapActivity", "JSON успешно получен и разобран")
        } else {
            Log.e("MapActivity", "Ошибка получения JSON")
            Toast.makeText(this, "Ошибка загрузки данных здания", Toast.LENGTH_LONG).show()
            return
        }

        val validRooms = listOf(
            (101..123),
            (201..227),
            (301..326),
            (401..428)
        ).flatMap { it.toList() }

        findButton.setOnClickListener {
            val endRoomId = roomNumberEditText.text.toString().trim()
            when {
                endRoomId.isEmpty() -> {
                    Toast.makeText(this, "Введите номер аудитории", Toast.LENGTH_SHORT).show()
                }
                endRoomId.length != 3 -> {
                    Toast.makeText(this, "Некорректный номер аудитории", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                endRoomId.first() !in '1'..'4' -> {
                    Toast.makeText(this, "Некорректный номер этажа", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                endRoomId.toInt() !in validRooms -> {
                    Toast.makeText(this, "Аудитория $endRoomId не найдена", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                else -> {
                    drawMapWithRoute(endRoomId)
                }
            }

            drawMapWithRoute(endRoomId)
        }
    }
    private fun getPoint(id: String, doors: Map<String, Door>, hallways: Map<String, Hallway>, startPosition: List<Int>?): Point? {
        return when {
            id == "startPosition" && startPosition != null -> Point(startPosition[0], startPosition[1])
            doors.containsKey(id) -> Point(doors[id]!!.position[0], doors[id]!!.position[1])
            hallways.containsKey(id) -> Point(hallways[id]!!.path[0][0], hallways[id]!!.path[0][1])
            else -> null
        }
    }
    private fun findNearestStaircase(x: Int, floor: Floor): String {
        // Определяем идентификаторы лестниц в зависимости от этажа
        val (leftStaircaseId, rightStaircaseId) = when (floor.id) {
            1 -> Pair("stairs_left", "stairs_right")
            2 -> Pair("stairs_left_lk2", "stairs_right_lk2")
            3 -> Pair("stairs_left_lk3", "stairs_right_lk3")
            4 -> Pair("stairs_left_lk4", "stairs_right_lk4")

            else -> Pair("HSL", "HSR")
        }

        // Получаем координаты лестниц из данных этажа
        val leftStaircase = floor.hallways[leftStaircaseId]?.path?.get(0)
        val rightStaircase = floor.hallways[rightStaircaseId]?.path?.get(0)
        Log.d("MapActivity", "Поиск ближайшей лестницы для x=$x")
        Log.d("MapActivity", "Левая лестница: $leftStaircase")
        Log.d("MapActivity", "Правая лестница: $rightStaircase")
        if (leftStaircase == null && rightStaircase == null) {
            Log.e("MapActivity", "Ни одна лестница не найдена на этаже ${floor.id}")
            // Проверяем все ключи в hallways, чтобы найти возможные лестницы
            val possibleStairs = floor.hallways.keys.filter {
                it.contains("stairs") || it.contains("STAIR") || it.contains("stair")
            }
            Log.d("MapActivity", "Возможные лестницы на этаже: $possibleStairs")

            // Если это верхний этаж, используем основной узел этажа
            if (floor.id > 1) {
                val floorMainNode = "H${floor.id}"
                Log.d("MapActivity", "Используем основной узел верхнего этажа: $floorMainNode")
                return floorMainNode
            }

            return possibleStairs.firstOrNull() ?: "H${floor.id}"
        }

        // Если одна из лестниц отсутствует, возвращаем ту, что существует
        if (leftStaircase == null) {
            Log.d("MapActivity", "Левая лестница отсутствует, используем правую")
            return rightStaircaseId
        }
        if (rightStaircase == null) {
            Log.d("MapActivity", "Правая лестница отсутствует, используем левую")
            return leftStaircaseId
        }
        // Определяем ближайшую лестницу
        val leftDistance = kotlin.math.abs(x - leftStaircase[0])
        val rightDistance = kotlin.math.abs(x - rightStaircase[0])
        Log.d("MapActivity", "Расстояние до левой лестницы: $leftDistance")
        Log.d("MapActivity", "Расстояние до правой лестницы: $rightDistance")

        return if (leftDistance < rightDistance) {
            Log.d("MapActivity", "Выбрана левая лестница $leftStaircaseId")
            leftStaircaseId
        } else {
            Log.d("MapActivity", "Выбрана правая лестница $rightStaircaseId")
            rightStaircaseId
        }
    }
    private fun findPath(floor: Floor, start: String, target: String): List<String> {
        Log.d("MapActivity", "Поиск пути на этаже ${floor.id} от $start до $target")

        // Проверяем, существуют ли начальная и конечная точки в данных
        val startExists = floor.connections.containsKey(start) ||
                floor.doors.containsKey(start) ||
                floor.hallways.containsKey(start) ||
                start == "startPosition"

        val targetExists = floor.connections.containsKey(target) ||
                floor.doors.containsKey(target) ||
                floor.hallways.containsKey(target)

        Log.d("MapActivity", "Начальная точка '$start' существует: $startExists")
        Log.d("MapActivity", "Конечная точка '$target' существует: $targetExists")

        // Если начальная точка не существует, но это верхний этаж, пробуем использовать основной узел этажа
        if (!startExists && floor.id > 1) {
            val floorMainNode = "H${floor.id}"
            if (floor.connections.containsKey(floorMainNode) || floor.hallways.containsKey(floorMainNode)) {
                Log.d("MapActivity", "Заменяем начальную точку '$start' на основной узел этажа '$floorMainNode'")
                return findPath(floor, floorMainNode, target)
            }
        }

        if (!startExists || !targetExists) {
            Log.e("MapActivity", "Начальная или конечная точка не существует в данных этажа ${floor.id}")
            return emptyList()
        }

        val queue: MutableList<List<String>> = mutableListOf(listOf(start))
        val visited: MutableSet<String> = mutableSetOf()

        var iterations = 0
        val maxIterations = 1000 // Защита от бесконечного цикла

        while (queue.isNotEmpty() && iterations < maxIterations) {
            iterations++
            val path = queue.removeAt(0)
            val node = path.last()

            if (node == target) {
                Log.d("MapActivity", "Путь найден за $iterations итераций: $path")
                return path
            }

            if (node !in visited) {
                visited.add(node)
                val neighbors = floor.connections[node] ?: emptyList()
                Log.d("MapActivity", "Узел: $node, соседи: $neighbors")

                for (neighbor in neighbors) {
                    if (neighbor !in visited) {
                        val newPath = path + neighbor
                        queue.add(newPath)
                    }
                }
            }
        }

        if (iterations >= maxIterations) {
            Log.e("MapActivity", "Превышено максимальное количество итераций при поиске пути")
        } else {
            Log.e("MapActivity", "Путь не найден. Посещено ${visited.size} узлов")
        }

        return emptyList()
    }


    private fun drawMapWithRoute(endRoomId: String) {
        try {
            // Ностройки и инициализация
            val buildingId = 0
            /*
            buildingId получаем из QR-кода, например:
              -0: lk,
              -1: gl_front,
              -2: gl_back,
              -3: rt,
              -4: nrg,
              -5: ubk,
              -6: gg,
              -7: him
           */
            val buildingsDict = mapOf(
                1 to "lk",
                2 to "gl_front",
                3 to "gl_back",
                4 to "rt",
                5 to "nrg",
                6 to "ubk",
                7 to "gg",
                8 to "him"
            )
            val someBuilding = building?.building
            val buildingTag = buildingsDict[buildingId + 1]


            Log.d("MapActivity", "Выбрано здание $buildingTag")


            //-----------------------------Получаем здание по id---------------------------------
            val currentBuilding = someBuilding?.get(buildingId) ?: return

            val currentBuildingName = currentBuilding.name
            Toast.makeText(this, "Здание $currentBuildingName выбрано", Toast.LENGTH_SHORT).show()

            val firstFloor = currentBuilding.floors.find { it.id == 1 } ?: return
            val floorNumber = endRoomId.firstOrNull()?.digitToIntOrNull()
            if (floorNumber == null) {
                Toast.makeText(this, "Некорректный номер аудитории", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Введена пустая строка или некорректный номер аудитории")
                return
            }
            val isHigherFloor = floorNumber > 1

            val firstFloorBitmap = loadSvgBitmap("${buildingTag}_1.svg") ?: return
            val firstFloorCanvas = Canvas(firstFloorBitmap)

            if (isHigherFloor) {
                val targetFloor = currentBuilding.floors.find { it.id == floorNumber } ?: return
                val endRoom = targetFloor.doors[endRoomId] ?: return

                val targetStaircase = findNearestStaircase(endRoom.position[0], targetFloor)
                val firstFloorStaircase = if (targetStaircase.contains("left")) "stairs_left" else "stairs_right"

                val firstFloorPath = findPath(firstFloor, "startPosition", firstFloorStaircase)
                val firstFloorPathPoints = firstFloorPath.mapNotNull {
                    getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition)
                }

                if (firstFloorPathPoints.isNotEmpty()) {
                    drawPath(firstFloorCanvas, firstFloorPathPoints)
                    floorMapImageView.setImageBitmap(firstFloorBitmap)
                    floorMapImageView.visibility = View.VISIBLE
                    if (buildingTag != null) {
                        drawHigherFloorRoute(buildingTag, targetFloor, targetStaircase, endRoomId)
                    }
                    else {
                        Toast.makeText(this, "Маршрут на первом этаже не найден", Toast.LENGTH_SHORT).show()
                        Log.d("MapActivity", "buildingTag не найден")
                    }
                } else {
                    Toast.makeText(this, "Маршрут на первом этаже не найден", Toast.LENGTH_SHORT).show()
                }
            } else {

                val pathIds = findPath(firstFloor, "startPosition", endRoomId)
                val pathPoints = pathIds.mapNotNull {
                    getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition)
                }
                if (pathPoints.isNotEmpty()) {
                    drawPath(firstFloorCanvas, pathPoints)
                    mapImageView.setImageBitmap(firstFloorBitmap)
                } else {
                    Toast.makeText(this, "Маршрут на 1 этаже не найден", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MapActivity", "Ошибка загрузки карты", e)
        }
    }
    private fun drawPath(canvas: Canvas, path: List<Point>) {
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 30f
            style = Paint.Style.STROKE
        }

        val androidPath = Path()
        if (path.isNotEmpty()) {
            androidPath.moveTo(path[0].x.toFloat(), path[0].y.toFloat())
            for (i in 1 until path.size) {
                androidPath.lineTo(path[i].x.toFloat(), path[i].y.toFloat())
            }
        }
        canvas.drawPath(androidPath, paint)
    }
    private fun drawHigherFloorRoute(buildingTag: String, floor: Floor, staircase: String, endRoomId: String) {
        try {
            val svgFileName = "${buildingTag}_${floor.id}.svg"
            val floorBitmap = loadSvgBitmap(svgFileName) ?: return
            val floorCanvas = Canvas(floorBitmap)

            // 🔴 Проверяем, существует ли аудитория 228 (или любая другая)
            if (!floor.doors.containsKey(endRoomId)) {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Аудитория $endRoomId отсутствует на этаже ${floor.id}")
                return  // 🚀 Выход из метода
            }

            val pathIds = findPath(floor, staircase, endRoomId)

            // 🔴 Проверяем, найден ли путь (если path пустой, маршрут не рисуется)
            if (pathIds.isEmpty()) {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Путь не найден на этаже ${floor.id} от $staircase до $endRoomId")
                return  // 🚀 Выход из метода
            }

            val pathPoints = pathIds.mapNotNull {
                getPoint(it, floor.doors, floor.hallways, floor.startPosition)
            }

            if (pathPoints.isNotEmpty()) {
                drawPath(floorCanvas, pathPoints)
                mapImageView.setImageBitmap(floorBitmap)
            } else {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Не удалось нарисовать путь на этаже ${floor.id}")
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки карты этажа", e)
        }
    }
    private fun loadSvgBitmap(svgFileName: String): Bitmap? {
        return try {
            assets.open(svgFileName).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                val picture = svg.renderToPicture()
                val drawable = PictureDrawable(picture)
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
            null
        }
    }
}
