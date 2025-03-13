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
                          { "id": "startRoomId", "x": 1950, "y": 2315 },
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
                          { "id": "111", "x": 220, "y": 1850 },
                          { "id": "112", "x": 220, "y": 1700 },
                          { "id": "113", "x": 350, "y": 1700 },
                          { "id": "114", "x": 500, "y": 1700 },
                          { "id": "115", "x": 830, "y": 1700 },
                          { "id": "116", "x": 1280, "y": 1700 },
                          { "id": "117", "x": 1440, "y": 1700 },
                          { "id": "118", "x": 1580, "y": 1700 },
                          { "id": "119", "x": 1740, "y": 1700 },
                          { "id": "120", "x": 1890, "y": 1700 },
                          { "id": "121", "x": 2270, "y": 1700 },
                          { "id": "122", "x": 2450, "y": 1700 },
                          { "id": "123", "x": 2740, "y": 1700 }
                    ],
                    "hallways": [
                          { "id": "H1", "x": 1950, "y": 1750 },
                          { "id": "H2", "x": 2500, "y": 1750 },
                          { "id": "H101", "x": 3200, "y": 1750 },
                          { "id": "H102", "x": 3100, "y": 1750 },
                          { "id": "H103", "x": 2920, "y": 1750 },
                          { "id": "H104", "x": 2920, "y": 1750 },
                          { "id": "H105", "x": 1750, "y": 1750 },
                          { "id": "H106", "x": 1250, "y": 1750 },
                          { "id": "H107", "x": 950, "y": 1750 },
                          { "id": "H108", "x": 800, "y": 1750 },
                          { "id": "H109", "x": 500, "y": 1750 },
                          { "id": "H110", "x": 350, "y": 1750 },
                          { "id": "H111", "x": 220, "y": 1750 },
                          { "id": "H112", "x": 220, "y": 1750 },
                          { "id": "H113", "x": 350, "y": 1750 },
                          { "id": "H114", "x": 500, "y": 1750 },
                          { "id": "H115", "x": 830, "y": 1750 },
                          { "id": "H116", "x": 1280, "y": 1750 },
                          { "id": "H117", "x": 1440, "y": 1750 },
                          { "id": "H118", "x": 1580, "y": 1750 },
                          { "id": "H119", "x": 1740, "y": 1750 },
                          { "id": "H120", "x": 1890, "y": 1750 },
                          { "id": "H121", "x": 2270, "y": 1750 },
                          { "id": "H122", "x": 2450, "y": 1750 },
                          { "id": "H123", "x": 2740, "y": 1750 },
                        ],
                    "connections": [
                          { "from": "startRoomId", "to": "H1" },
                          { "from": "H1", "to": "H2" },
                          { "from": "H2", "to": "H101" },
                          { "from": "H1", "to": "H102" },
                          { "from": "H1", "to": "H103" },
                          { "from": "H1", "to": "H104" },
                          { "from": "H1", "to": "H105" },
                          { "from": "H1", "to": "H106" },
                          { "from": "H1", "to": "H107" },
                          { "from": "H1", "to": "H108" },
                          { "from": "H1", "to": "H109" },
                          { "from": "H1", "to": "H110" },
                          { "from": "H1", "to": "H111" },
                          { "from": "H1", "to": "H112" },
                          { "from": "H1", "to": "H113" },
                          { "from": "H1", "to": "H114" },
                          { "from": "H1", "to": "H115" },
                          { "from": "H1", "to": "H116" },
                          { "from": "H1", "to": "H117" },
                          { "from": "H1", "to": "H118" },
                          { "from": "H1", "to": "H119" },
                          { "from": "H1", "to": "H120" },
                          { "from": "H1", "to": "H121" },
                          { "from": "H1", "to": "H122" },
                          { "from": "H1", "to": "H123" },
                          { "from": "H101", "to": "101" },
                          { "from": "H102", "to": "102" },
                          { "from": "H103", "to": "103" },
                          { "from": "H104", "to": "104" },
                          { "from": "H105", "to": "105" },
                          { "from": "H106", "to": "106" },
                          { "from": "H107", "to": "107" },
                          { "from": "H108", "to": "108" },
                          { "from": "H109", "to": "109" },
                          { "from": "H110", "to": "110" },
                          { "from": "H111", "to": "111" },
                          { "from": "H112", "to": "112" },
                          { "from": "H113", "to": "113" },
                          { "from": "H114", "to": "114" },
                          { "from": "H115", "to": "115" },
                          { "from": "H116", "to": "116" },
                          { "from": "H117", "to": "117" },
                          { "from": "H118", "to": "118" },
                          { "from": "H119", "to": "119" },
                          { "from": "H120", "to": "120" },
                          { "from": "H121", "to": "121" },
                          { "from": "H122", "to": "122" },
                          { "from": "H123", "to": "123" }
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