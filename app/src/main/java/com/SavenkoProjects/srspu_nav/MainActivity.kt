package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.io.InputStream

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
            val json = loadJSONFromAsset("building_data.json")

            if (json != null) {
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
            } else {
                tvResult?.text = "Ошибка загрузки JSON файла"
            }
        }
    }

    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream: InputStream = assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            Log.e("MainActivity", "Ошибка загрузки JSON файла: ${ex.message}", ex)
            null
        }
    }

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