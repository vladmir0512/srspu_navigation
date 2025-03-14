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
            drawMapWithRoute(endRoomId)
        }
    }

    private fun drawMapWithRoute(endRoomId: String) {
        try {
            val inputStream: InputStream = assets.open("lk_1.svg")
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

            val floor = building?.building?.floors?.get(0)
            if (floor == null) {
                Toast.makeText(this, "Данные о этаже не найдены", Toast.LENGTH_SHORT).show()
                return
            }

            val startValue = "startPosition"
            val startPosition = floor.startPosition
            val endRoom = floor.doors[endRoomId]

            if (startPosition == null || endRoom == null) {
                Toast.makeText(this, "Аудитория не найдена", Toast.LENGTH_SHORT).show()
                return
            }

            val pathIds = findPath(floor, startValue, endRoomId)
            val pathPoints = pathIds.mapNotNull { getPoint(it, floor.doors, floor.hallways, floor.startPosition) }

            if (pathPoints.isNotEmpty()) {
                drawPath(rotatedCanvas, pathPoints)
                mapImageView.setImageBitmap(rotatedBitmap)
            } else {
                Toast.makeText(this, "Маршрут не найден", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
        }
    }

    fun findPath(floor: Floor, start: String, target: String): List<String> {
        val queue: MutableList<List<String>> = mutableListOf(listOf(start))
        val visited: MutableSet<String> = mutableSetOf()

        while (queue.isNotEmpty()) {
            val path = queue.removeAt(0)
            val node = path.last()

            if (node == target) return path

            if (node !in visited) {
                visited.add(node)
                val neighbors = floor.connections[node] ?: emptyList()
                for (neighbor in neighbors) {
                    val newPath = path + neighbor
                    queue.add(newPath)
                }
            }
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
}
