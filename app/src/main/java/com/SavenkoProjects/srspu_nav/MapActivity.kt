package com.SavenkoProjects.srspu_nav

import android.graphics.*
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream

class MapActivity : AppCompatActivity() {
    private var building: Building? = null
    private lateinit var mapImageView: ImageView
    private lateinit var roomNumberEditText: EditText
    private lateinit var findButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        roomNumberEditText = findViewById(R.id.roomNumberEditText)
        findButton = findViewById(R.id.findButton)
        mapImageView = findViewById(R.id.mapImageView)

        val json = intent.getStringExtra("buildingJson")
        if (json != null) {
            building = Gson().fromJson(json, Building::class.java)
            Log.d("MapActivity", "JSON успешно получен и разобран: $json")
        } else {
            Log.e("MapActivity", "Ошибка получения JSON")
            Toast.makeText(this, "Ошибка загрузки данных здания", Toast.LENGTH_LONG).show()
            return
        }

        findButton.setOnClickListener {
            val endRoomId = roomNumberEditText.text.toString().trim()
            if (endRoomId.isEmpty()) {
                Toast.makeText(this, "Введите номер аудитории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            drawMapWithRoute(endRoomId)
        }
    }

    private fun drawMapWithRoute(endRoomId: String) {
        try {
            // Определяем этаж по первой цифре номера аудитории
            val floorNumber = endRoomId.first().toString().toInt()
            val isHigherFloor = floorNumber > 1

            // Загружаем SVG карту первого этажа
            val firstFloorSvgFileName = "lk_1.svg"
            val inputStream: InputStream = assets.open(firstFloorSvgFileName)
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

            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            val rotatedCanvas = Canvas(rotatedBitmap)

            // Получаем данные первого этажа
            val firstFloor = building?.building?.floors?.find { it.id == 1 }
            if (firstFloor == null) {
                Toast.makeText(this, "Данные о первом этаже не найдены", Toast.LENGTH_SHORT).show()
                return
            }

            val startValue = "startPosition"

            if (firstFloor.startPosition == null) {
                Toast.makeText(this, "Начальная позиция не найдена", Toast.LENGTH_SHORT).show()
                return
            }

            if (isHigherFloor) {
                // Получаем данные целевого этажа
                val targetFloor = building?.building?.floors?.find { it.id == floorNumber }
                if (targetFloor == null) {
                    Toast.makeText(this, "Данные о этаже $floorNumber не найдены", Toast.LENGTH_SHORT).show()
                    return
                }

                val endRoom = targetFloor.doors[endRoomId]
                if (endRoom == null) {
                    Toast.makeText(this, "Аудитория $endRoomId не найдена", Toast.LENGTH_SHORT).show()
                    return
                }

                // Находим ближайшую лестницу на целевом этаже
                val targetStaircase = findNearestStaircase(endRoom.position[0], targetFloor)
                
                // Находим соответствующую лестницу на первом этаже
                val firstFloorStaircase = if (targetStaircase.contains("left")) "HSL" else "HSR"

                // Рисуем путь на первом этаже до лестницы
                val firstFloorPathIds = findPath(firstFloor, startValue, firstFloorStaircase)
                val firstFloorPathPoints = firstFloorPathIds.mapNotNull { 
                    getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition) 
                }

                if (firstFloorPathPoints.isNotEmpty()) {
                    drawPath(rotatedCanvas, firstFloorPathPoints)
                    
                    // Сохраняем изображение первого этажа с маршрутом во временный битмап
                    val firstFloorBitmap = rotatedBitmap.config?.let { rotatedBitmap.copy(it, true) }
                    
                    // Теперь рисуем путь на целевом этаже от лестницы до аудитории
                    drawHigherFloorRoute(floorNumber, targetStaircase, endRoomId)
                    
                    // Отображаем маршрут первого этажа в маленьком ImageView
                    val firstFloorImageView = findViewById<ImageView>(R.id.floorMapImageView)
                    firstFloorImageView.setImageBitmap(firstFloorBitmap)
                } else {
                    Toast.makeText(this, "Маршрут на первом этаже не найден", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Для первого этажа - обычный маршрут до аудитории
                val endRoom = firstFloor.doors[endRoomId]
                if (endRoom == null) {
                    Toast.makeText(this, "Аудитория не найдена", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val pathIds = findPath(firstFloor, startValue, endRoomId)
                val pathPoints = pathIds.mapNotNull { 
                    getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition) 
                }

                if (pathPoints.isNotEmpty()) {
                    drawPath(rotatedCanvas, pathPoints)
                    mapImageView.setImageBitmap(rotatedBitmap)
                } else {
                    Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
        }
    }

    fun findPath(floor: Floor, start: String, target: String): List<String> {
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

    private fun getPoint(id: String, doors: Map<String, Door>, hallways: Map<String, Hallway>, startPosition: List<Int>?): Point? {
        return when {
            id == "startPosition" && startPosition != null -> Point(startPosition[0], startPosition[1])
            doors.containsKey(id) -> Point(doors[id]!!.position[0], doors[id]!!.position[1])
            hallways.containsKey(id) -> Point(hallways[id]!!.path[0][0], hallways[id]!!.path[0][1])
            else -> null
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

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun findNearestStaircase(x: Int, floor: Floor): String {
        // Определяем идентификаторы лестниц в зависимости от этажа
        val (leftStaircaseId, rightStaircaseId) = when (floor.id) {
            2 -> Pair("stairs_left_lk2", "stairs_right_lk2")
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
        val leftDistance = Math.abs(x - leftStaircase[0])
        val rightDistance = Math.abs(x - rightStaircase[0])
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
    private fun drawHigherFloorRoute(floorNumber: Int, staircase: String, endRoomId: String) {
        try {
            // Загружаем SVG карту соответствующего этажа
            val svgFileName = when (floorNumber) {
                2 -> "lk_2.svg"
                3 -> "lk_3.svg"
                4 -> "lk_4.svg"
                else -> return
            }
            
            Log.d("MapActivity", "Загрузка SVG карты для этажа $floorNumber: $svgFileName")
            val inputStream: InputStream = assets.open(svgFileName)
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

            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            val rotatedCanvas = Canvas(rotatedBitmap)

            // Получаем данные этажа
            val floor = building?.building?.floors?.find { it.id == floorNumber }
            if (floor == null) {
                Toast.makeText(this, "Данные о этаже $floorNumber не найдены", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Данные о этаже $floorNumber не найдены в объекте building")
                return
            }
            
            // Используем основной узел этажа вместо лестницы, если лестница не найдена
            // Например, H2 для 2 этажа, H3 для 3 этажа и т.д.
            val floorMainNode = "H$floorNumber"
            val startNode = if (floor.connections.containsKey(staircase)) {
                staircase
            } else {
                Log.d("MapActivity", "Лестница $staircase не найдена на этаже $floorNumber, используем $floorMainNode")
                floorMainNode
            }
            
            // Логируем данные о лестницах и соединениях для отладки
            Log.d("MapActivity", "Начальный узел для маршрута на этаже $floorNumber: $startNode")
            Log.d("MapActivity", "Целевая аудитория: $endRoomId")
            Log.d("MapActivity", "Доступные соединения для начального узла: ${floor.connections[startNode]}")
            Log.d("MapActivity", "Аудитория существует: ${floor.doors.containsKey(endRoomId)}")
            Log.d("MapActivity", "Все узлы на этаже: ${floor.hallways.keys}")
            
            // Проверяем наличие аудитории в данных этажа
            if (!floor.doors.containsKey(endRoomId)) {
                Toast.makeText(this, "Аудитория $endRoomId не найдена на этаже $floorNumber", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Аудитория $endRoomId отсутствует в данных этажа $floorNumber")
                return
            }

            // Проверяем существование начального узла
            if (!floor.connections.containsKey(startNode) && !floor.hallways.containsKey(startNode)) {
                Log.e("MapActivity", "Начальный узел $startNode не существует на этаже $floorNumber")
                Log.d("MapActivity", "Доступные узлы в connections: ${floor.connections.keys}")
                Log.d("MapActivity", "Доступные узлы в hallways: ${floor.hallways.keys}")
                
                // Пробуем найти любой доступный узел в качестве начальной точки
                val alternativeStartNode = floor.connections.keys.firstOrNull() ?: 
                                          floor.hallways.keys.firstOrNull() ?: "startPosition"
                
                Log.d("MapActivity", "Используем альтернативный узел: $alternativeStartNode")
                if (alternativeStartNode != startNode) {
                    // Рекурсивно вызываем эту же функцию с новым начальным узлом
                    drawHigherFloorRoute(floorNumber, alternativeStartNode, endRoomId)
                    return
                }
            }

            // Рисуем путь от начального узла до аудитории
            val pathIds = findPath(floor, startNode, endRoomId)
            Log.d("MapActivity", "Найденный путь: $pathIds")
            
            val pathPoints = pathIds.mapNotNull { 
                getPoint(it, floor.doors, floor.hallways, floor.startPosition) 
            }
            Log.d("MapActivity", "Точки пути: $pathPoints")

            if (pathPoints.isNotEmpty()) {
                drawPath(rotatedCanvas, pathPoints)
                // Отображаем карту верхнего этажа с маршрутом в основном ImageView
                mapImageView.setImageBitmap(rotatedBitmap)
                
                // Показываем маршрут первого этажа в маленьком ImageView
                val firstFloorImageView = findViewById<ImageView>(R.id.floorMapImageView)
                firstFloorImageView.visibility = View.VISIBLE
                Log.d("MapActivity", "Маршрут успешно отображен на этаже $floorNumber")
            } else {
                Log.e("MapActivity", "Не удалось найти маршрут на этаже $floorNumber от $startNode до $endRoomId")
                Log.e("MapActivity", "Данные соединений для начального узла: ${floor.connections[startNode]}")
                Log.e("MapActivity", "Все соединения на этаже: ${floor.connections}")
                
                // Проверяем, есть ли прямой путь от H к аудитории
                val directPathIds = floor.connections[floorMainNode]?.filter { it == endRoomId || it.contains(endRoomId) }
                if (!directPathIds.isNullOrEmpty()) {
                    Log.d("MapActivity", "Найден прямой путь от $floorMainNode к $endRoomId через ${directPathIds.first()}")
                    
                    // Рисуем прямой путь
                    val directPathPoints = listOf(
                        getPoint(floorMainNode, floor.doors, floor.hallways, floor.startPosition),
                        getPoint(directPathIds.first(), floor.doors, floor.hallways, floor.startPosition),
                        getPoint(endRoomId, floor.doors, floor.hallways, floor.startPosition)
                    ).filterNotNull()
                    
                    if (directPathPoints.isNotEmpty()) {
                        drawPath(rotatedCanvas, directPathPoints)
                        mapImageView.setImageBitmap(rotatedBitmap)
                        return
                    }
                }
                
                Toast.makeText(this, "Маршрут на ${floorNumber} этаже не найден. Проверьте данные соединений.", Toast.LENGTH_LONG).show()
                
                // Продолжаем использовать SVG, но без маршрута
                mapImageView.setImageBitmap(rotatedBitmap)
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG для этажа $floorNumber", e)
            Toast.makeText(this, "Ошибка загрузки карты этажа $floorNumber: ${e.message}", Toast.LENGTH_LONG).show()
            
            try {
                // Пробуем загрузить PNG карту как запасной вариант
                val mapResId = when (floorNumber) {
                    2 -> R.drawable.lk_floor2_map
                    3 -> R.drawable.lk_floor3_map
                    4 -> R.drawable.lk_floor4_map
                    else -> return
                }
                mapImageView.setImageResource(mapResId)
                Log.d("MapActivity", "Загружена PNG карта для этажа $floorNumber как запасной вариант")
            } catch (e2: Exception) {
                Log.e("MapActivity", "Не удалось загрузить даже PNG карту", e2)
            }
        }
    }

    // Этот метод теперь не используется, так как мы напрямую устанавливаем изображения в ImageView
    private fun showFloorMap(floorNumber: Int, bitmap: Bitmap? = null) {
        try {
            // Отображаем карту этажа
            val floorMapImageView = findViewById<ImageView>(R.id.floorMapImageView)
            
            if (bitmap != null) {
                // Используем переданный битмап с нарисованным маршрутом
                floorMapImageView.setImageBitmap(bitmap)
            } else {
                // Загружаем стандартную карту этажа
                val mapResId = when (floorNumber) {
                    2 -> R.drawable.lk_floor2_map
                    3 -> R.drawable.lk_floor3_map
                    4 -> R.drawable.lk_floor4_map
                    else -> return
                }
                floorMapImageView.setImageResource(mapResId)
            }
            
            floorMapImageView.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("MapActivity", "Ошибка загрузки карты этажа", e)
        }
    }
}
