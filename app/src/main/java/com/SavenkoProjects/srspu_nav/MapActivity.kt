package com.SavenkoProjects.srspu_nav

import android.graphics.*
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
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

            Log.d("MapActivity", "Ищем маршрут к аудитории $endRoomId")
            drawMapWithRoute(endRoomId)
        }
    }

    private fun drawMapWithRoute(endRoomId: String) {
        try {
            val inputStream: InputStream = assets.open("lk_1.svg")
            val svg = SVG.getFromInputStream(inputStream)
            val picture = svg.renderToPicture()
            val drawable = PictureDrawable(picture)
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            val rotatedCanvas = Canvas(rotatedBitmap)

            val startRoomId = "startRoomId"
            val startRoom = building?.building?.doors?.find { it.id.toString() == startRoomId }
            val endRoom = building?.building?.doors?.find { it.id.toString() == endRoomId }

            if (startRoom == null || endRoom == null) {
                Log.e("MapActivity", "Не найдены координаты одной из комнат: startRoomId=$startRoomId, endRoomId=$endRoomId")
                Toast.makeText(this, "Аудитория не найдена", Toast.LENGTH_SHORT).show()
                return
            }

            val path = findPath(startRoomId, endRoomId)
            if (path.isNotEmpty()) {
                drawPath(rotatedCanvas, path)
                mapImageView.setImageBitmap(rotatedBitmap)
            } else {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.d("MapActivity", "Маршрут не найден path: $path rotatedCanvas: $rotatedCanvas")
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
        }
    }

    private fun findPath(startRoomId: String, endRoomId: String): List<Point> {
        val connections = building?.building?.connections ?: return emptyList()
        val doors = building?.building?.doors ?: return emptyList()
        val hallways = building?.building?.hallways ?: return emptyList()

        val queue = mutableListOf<Pair<String, List<Point>>>()
        val visited = mutableSetOf<String>()

        val startPoint = getPoint(startRoomId, doors, hallways)
        if (startPoint == Point(0, 0)) {
            Log.e("MapActivity", "Начальная комната ($startRoomId) не найдена")
            return emptyList()
        }

        queue.add(Pair(startRoomId, listOf(startPoint)))
        visited.add(startRoomId)

        Log.d("MapActivity", "Начало поиска маршрута. Очередь: ${queue.size}")

        while (queue.isNotEmpty()) {
            val (currentId, currentPath) = queue.removeAt(0)
            Log.d("MapActivity", "Проверяем: $currentId, Путь: ${currentPath.map { "(${it.x}, ${it.y})" }}")

            if (currentId == endRoomId) {
                Log.d("MapActivity", "Маршрут найден: ${currentPath.map { "(${it.x}, ${it.y})" }}")
                return currentPath
            }

            connections.filter { it.from == currentId }.forEach { connection ->
                Log.d("MapActivity", "Проверяем соединение: ${connection.from} -> ${connection.to}")
                if (!visited.contains(connection.to)) {
                    visited.add(connection.to)
                    val nextPoint = getPoint(connection.to, doors, hallways)
                    queue.add(Pair(connection.to, currentPath + nextPoint))
                    Log.d("MapActivity", "Добавляем в очередь: ${connection.to}, Путь: ${ (currentPath + nextPoint).map { "(${it.x}, ${it.y})" }}")
                }
            }
        }
        Log.d("MapActivity", "Маршрут не найден")
        return emptyList()
    }
    private fun getPoint(id: String, doors: List<Door>, hallways: List<Hallway>): Point {
        doors.find { it.id.toString() == id }?.let {
            Log.d("MapActivity", "Найдена дверь: $id (${it.x}, ${it.y})")
            return Point(it.x, it.y)
        }
        hallways.find { it.id == id }?.let {
            Log.d("MapActivity", "Найден коридор: $id (${it.x}, ${it.y})")
            return Point(it.x, it.y)
        }
        Log.e("MapActivity", "Точка не найдена: $id")
        return Point(0, 0)
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
}