package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
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
            val json = loadJSONFromAsset("building_data.json")

            if (json != null) {
                try {
                    val building = parseJson(json)
                    if (building != null) {
                        val intent = Intent(this, MapActivity::class.java).apply {
                            putExtra("buildingJson", Gson().toJson(building))
                        }
                        startActivity(intent)
                    } else {
                        tvResult?.text = "Ошибка парсинга JSON или неполные данные"
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
}
