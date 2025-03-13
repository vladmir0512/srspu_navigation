package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class MainActivity : AppCompatActivity() {

    private var btnScanQR: Button? = null
    private var tvResult: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanQR = findViewById(R.id.btnScanQR)
        tvResult = findViewById(R.id.tvResult)

        btnScanQR?.setOnClickListener {
            Log.d("MainActivity", "Кнопка 'Найти' нажата")
            val json = """
                {
                  "building": {
                    "name": "ЛК (Лабораторный корпус)",
                    "doors": [
                      { "id": "startRoomId", "x": 1950, "y": 2350 },
                      { "id": "101", "x": 3200, "y": 1700 },
                      { "id": "102", "x": 3100, "y": 1850 },
                      { "id": "103", "x": 2950, "y": 2150 },
                      { "id": "104", "x": 2950, "y": 2150 },
                      { "id": "105", "x": 1750, "y": 1850 },
                      { "id": "106", "x": 1250, "y": 1850 },
                      { "id": "107", "x": 950, "y": 1850 },
                      { "id": "108", "x": 800, "y": 1850 },
                      { "id": "109", "x": 500, "y": 1850 },
                      { "id": "110", "x": 350, "y": 1850 },
                      { "id": "111", "x": 200, "y": 1850 },
                      { "id": "112", "x": 200, "y": 1700 },
                      { "id": "113", "x": 350, "y": 1700 },
                      { "id": "114", "x": 500, "y": 1700 },
                      { "id": "115", "x": 800, "y": 1700 },
                      { "id": "116", "x": 1280, "y": 1700 },
                      { "id": "117", "x": 1450, "y": 1700 },
                      { "id": "118", "x": 1570, "y": 1700 },
                      { "id": "119", "x": 1750, "y": 1700 },
                      { "id": "120", "x": 1880, "y": 1700 },
                      { "id": "121", "x": 2290, "y": 1700 },
                      { "id": "122", "x": 2450, "y": 1700 },
                      { "id": "123", "x": 2750, "y": 1700 }
                    ]
                  }
                }
            """.trimIndent()

            try {
                building = parseJson(json)
                if (building != null) {
                    val buildingJson = Gson().toJson(building)
                    Log.d("MainActivity", "Передаем JSON в MapActivity: $buildingJson")

                    val intent = Intent(this, MapActivity::class.java).apply {
                        putExtra("buildingJson", buildingJson)
                    }
                    startActivity(intent)
                } else {
                    tvResult?.text = "Ошибка парсинга JSON"
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка: ${e.message}", e)
                tvResult?.text = "Ошибка"
            }
        }
    }

//    private fun showBuildingInfo(endRoomId: String) {
//        Log.d("MainActivity", "showBuildingInfo вызван с endRoomId: $endRoomId")
//        try {
//            val intent = Intent(this, MapActivity::class.java).apply {
//                putExtra("buildingJson", Gson().toJson(building))
//                putExtra("endRoomId", endRoomId)
//            }
//            startActivity(intent)
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Ошибка при запуске MapActivity: ${e.message}", e)
//            tvResult?.text = "Ошибка при запуске MapActivity"
//        }
//    }

    private fun parseJson(json: String): Building? {
        try {
            val gson = Gson()
            val building = gson.fromJson(json, Building::class.java)
            Log.d("MainActivity", "Parsed building: $building")
            return building
        } catch (e: JsonSyntaxException) {
            Log.e("MainActivity", "Ошибка парсинга JSON: ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e("MainActivity", "Непредвиденная ошибка при парсинге JSON: ${e.message}", e)
            return null
        }
    }

    companion object {
        var building: Building? = null
    }
}