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

        // Инициализация UI элементов
        roomNumberEditText = findViewById(R.id.roomNumberEditText)
        findButton = findViewById(R.id.findButton)
        mapImageView = findViewById(R.id.mapImageView)

        // Получение JSON с данными здания из Intent
        val json = intent.getStringExtra("buildingJson")
        if (json != null) {
            building = Gson().fromJson(json, Building::class.java)
            Log.d("MapActivity", "JSON успешно получен и разобран")
        } else {
            Log.e("MapActivity", "Ошибка получения JSON")
            Toast.makeText(this, "Ошибка загрузки данных здания", Toast.LENGTH_LONG).show()
            return
        }

        // Обработчик кнопки "Найти"
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
            // Читаем карту из assets
            val inputStream: InputStream = assets.open("lk_1.svg")
            val svg = SVG.getFromInputStream(inputStream)
            val picture = svg.renderToPicture()
            val drawable = PictureDrawable(picture)
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            // Поворот карты на 90 градусов, если необходимо
            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            val rotatedCanvas = Canvas(rotatedBitmap)

            val startRoomId = "startRoomId" // ID стартовой точки (замени на нужный)

            // Проверяем, есть ли аудитория в JSON
            val endRoom = building?.building?.doors?.find { it.id.toString() == endRoomId }
            if (endRoom != null) {
                Log.d("MapActivity", "Рисуем маршрут: startRoomId -> $endRoomId")
                drawLineBetweenRooms(rotatedCanvas, startRoomId, endRoomId)
            } else {
                Log.e("MapActivity", "Комната $endRoomId не найдена в JSON")
                Toast.makeText(this, "Комната не найдена", Toast.LENGTH_SHORT).show()
            }

            mapImageView.setImageBitmap(rotatedBitmap)
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
        }
    }

    private fun drawLineBetweenRooms(canvas: Canvas, startRoomId: String, endRoomId: String) {
        val startRoom = building?.building?.doors?.find { it.id.toString() == startRoomId }
        val endRoom = building?.building?.doors?.find { it.id.toString() == endRoomId }

        if (startRoom == null || endRoom == null) {
            Log.e("MapActivity", "Не найдены координаты одной из комнат")
            return
        }

        Log.d("MapActivity", "Рисуем линию от (${startRoom.x}, ${startRoom.y}) до (${endRoom.x}, ${endRoom.y})")

        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 30f
        }
        canvas.drawLine(
            startRoom.x.toFloat(), startRoom.y.toFloat(),
            endRoom.x.toFloat(), endRoom.y.toFloat(),
            paint
        )
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
