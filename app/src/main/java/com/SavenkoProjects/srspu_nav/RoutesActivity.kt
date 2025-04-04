package com.SavenkoProjects.srspu_nav

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import androidx.core.graphics.toColorInt

class RoutesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoutesBinding
    private var isSearchVisible = false
    private var building: Building? = null
    private var isFirstState = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val json = loadJSONFromAsset().toString()
        binding = ActivityRoutesBinding.inflate(layoutInflater)
        binding.mapImageView.visibility = View.GONE;
        setContentView(binding.root)
        //---------------Получаем данные из Intent от SearchActivity---------------
        val buildingId = 0
        val searchText = intent.getStringExtra("searchText").toString()
        building = parseJson(json)


        Log.d("RoutesActivity", "searchText: $searchText")
        Log.d("RoutesActivity", "building: $building")
        if (building != null) {
                drawMapWithRoute(
                    buildingId = buildingId,
                    building = building!!,
                    endRoomId = searchText
                )
        }
        binding.searchButton.setOnClickListener{
            toggleSearchField()

        }
        binding.floorMapImageViewBack.setOnClickListener {
            rotateImageView(binding.floorMapImageViewBack);
        }
    }
    private fun rotateImageView(imageView: View){
        val animator: ObjectAnimator  = ObjectAnimator.ofFloat(
            imageView,
            "rotationX",
            0f, 180f
        );
        // Добавляем слушатель для анимации
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (isFirstState){
                    binding.floorMapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.floorMapImageView.visibility = View.GONE
                        }
                }
                else{
                    binding.mapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.mapImageView.visibility = View.GONE
                        }
                }

            }
            override fun onAnimationEnd(animation: Animator) {
                if (!isFirstState) {
                    // Возвращаем все обратно
                    binding.floorMapImageView.visibility = View.VISIBLE
                    binding.floorMapImageView.animate().alpha(1f).setDuration(300).start()
                }
                else{
                    binding.mapImageView.visibility = View.VISIBLE
                    binding.mapImageView.animate().alpha(1f).setDuration(300).start()
                }
                // Переключаем состояние
                isFirstState = !isFirstState
            }
            override fun onAnimationCancel(animation: Animator) {
                if (isFirstState){
                    binding.floorMapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.floorMapImageView.visibility = View.GONE
                        }
                }
                else{
                    binding.mapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.mapImageView.visibility = View.GONE
                        }
                }            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.setDuration(1500)
        // Запускаем анимацию
        animator.start()
    }
    private fun toggleSearchField() {
        if (isSearchVisible) {
            binding.searchEditText.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.searchEditText.visibility = View.GONE
                }
        } else {
            binding.searchEditText.visibility = View.VISIBLE
            binding.searchEditText.animate().alpha(1f).setDuration(300).start()
        }
        isSearchVisible = !isSearchVisible
    }

    private fun loadJSONFromAsset(): String? {
        return try {
            val inputStream: InputStream = assets.open("building_data.json")
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            Log.e("MainActivity", "Ошибка загрузки JSON файла: ${ex.message}", ex)
            null
        }
    }

    private fun parseJson(json: String): Building? {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Building>() {}.type
            gson.fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            Log.e("MainActivity", "Ошибка парсинга JSON: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("MainActivity", "Непредвиденная ошибка при парсинге JSON: ${e.message}", e)
            null
        }
    }

    private fun drawMapWithRoute(buildingId: Int, endRoomId: String, building: Building) {
        try {
            /*
            buildingId получаем из QR-кода, например:
              -0: lk,
              -1: gl_front,
              -2: gl_back,
              -3: rt,
              -4: nrg,
              -5: ubk, //TODO проверить SVG ubk
              -6: gg,
              -7: him
           */
            //-----------------------------Словарь зданий---------------------------------
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
            //-----------------------------Получаем здание как объект---------------------------------
            val someBuilding = building.building
            val buildingTag = buildingsDict[buildingId + 1]
            Log.d("MapActivity", "Тег выбранного здания $buildingTag")

            //-----------------------------Получаем здание по id---------------------------------
            val currentBuilding = someBuilding.get(buildingId) ?: return
            val currentBuildingName = currentBuilding.name
            Toast.makeText(this, "Здание $currentBuildingName выбрано", Toast.LENGTH_SHORT).show()

            //-----------------------------Получаем этаж по номеру---------------------------------
            val floorNumber = endRoomId.firstOrNull()?.digitToIntOrNull()
            val isHigherFloor = floorNumber!! > 1
            //-----------------------------Получаем первый этаж---------------------------------
            val firstFloor = currentBuilding.floors.find { it.id == 1 } ?: return
            val firstFloorBitmap = loadSvgBitmap("${buildingTag}_1.svg") ?: return
            val firstFloorCanvas = Canvas(firstFloorBitmap)

            if (isHigherFloor) {
                drawRouteOnHigherFloor(currentBuilding, floorNumber, endRoomId, firstFloor, firstFloorCanvas, firstFloorBitmap, buildingTag)
            } else {
                drawRouteOnFirstFloor(firstFloor, endRoomId, firstFloorCanvas, firstFloorBitmap)
            }
        } catch (e: Exception) {
            Log.e("MapActivity", "Ошибка загрузки карты", e)
        }
    }
    private fun loadSvgBitmap(svgFileName: String): Bitmap? {
        return try {
            val svg = SVG.getFromAsset(this.assets, svgFileName)
            val bitmap = createBitmap(svg.documentWidth.toInt(), svg.documentHeight.toInt())
            val canvas = Canvas(bitmap)
            svg.renderToCanvas(canvas)
            bitmap
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
            null
        }
    }
    private fun drawRouteOnHigherFloor(currentBuilding: BuildingData, floorNumber: Int, endRoomId: String, firstFloor: Floor, firstFloorCanvas: Canvas, firstFloorBitmap: Bitmap, buildingTag: String?) {
        val targetFloor = currentBuilding.floors.find { it.id == floorNumber } ?: return
        val endRoom = targetFloor.doors[endRoomId] ?: return
        val targetStaircase = findNearestStaircase(endRoom.position[0], targetFloor)
        val firstFloorStaircase =
            if (targetStaircase.contains("left")) "stairs_left" else "stairs_right"
        val firstFloorPath = findPath(firstFloor, "startPosition", firstFloorStaircase)
        val firstFloorPathPoints = firstFloorPath.mapNotNull {
            getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition)
        }
        if (firstFloorPathPoints.isNotEmpty()) {

            if (buildingTag != null) {
                drawPath(firstFloorCanvas, firstFloorPathPoints)
                binding.mapImageView.setImageBitmap(firstFloorBitmap)
                drawHigherFloorRoute(buildingTag, targetFloor, targetStaircase, endRoomId)
            } else {
                Toast.makeText(
                    this,
                    "Маршрут на первом этаже не найден",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("MapActivity", "buildingTag не найден")
            }
        } else {
            Toast.makeText(this, "Маршрут на первом этаже не найден", Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun drawRouteOnFirstFloor(firstFloor: Floor, endRoomId: String, firstFloorCanvas: Canvas, firstFloorBitmap: Bitmap) {
        val pathIds = findPath(firstFloor, "startPosition", endRoomId)
        val pathPoints = pathIds.mapNotNull {
            getPoint(it, firstFloor.doors, firstFloor.hallways, firstFloor.startPosition)
        }
        if (pathPoints.isNotEmpty()) {
            drawPath(firstFloorCanvas, pathPoints)
            binding.floorMapImageView.setImageBitmap(firstFloorBitmap)
            Toast.makeText(this, "Маршрут на 1 этаже не найден", Toast.LENGTH_SHORT).show()
        }
    }
    private fun drawHigherFloorRoute(buildingTag: String, floor: Floor, staircase: String, endRoomId: String) {
        try {
            val svgFileName = "${buildingTag}_${floor.id}.svg"
            val floorBitmap = loadSvgBitmap(svgFileName) ?: return
            val floorCanvas = Canvas(floorBitmap)

            //  Проверяем, существует ли аудитория
            if (!floor.doors.containsKey(endRoomId)) {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Аудитория $endRoomId отсутствует на этаже ${floor.id}")
                return  // 🚀 Выход из метода
            }
            val pathIds = findPath(floor, staircase, endRoomId)
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
                binding.floorMapImageView.setImageBitmap(floorBitmap)
            } else {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Не удалось нарисовать путь на этаже ${floor.id}")
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки карты этажа", e)
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
    private fun getPoint(id: String, doors: Map<String, Door>, hallways: Map<String, Hallway>, startPosition: List<Int>?): Point? {
        return when {
            id == "startPosition" && startPosition != null -> Point(startPosition[0], startPosition[1])
            doors.containsKey(id) -> Point(doors[id]!!.position[0], doors[id]!!.position[1])
            hallways.containsKey(id) -> Point(hallways[id]!!.path[0][0], hallways[id]!!.path[0][1])
            else -> null
        }
    }
    @SuppressLint("UseKtx")
    private fun drawPath(canvas: Canvas, path: List<Point>) {
        val paint = Paint().apply {
            color = "#BFFF5151".toColorInt()
            strokeWidth = 25f
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


}