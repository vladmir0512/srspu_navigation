package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import com.SavenkoProjects.srspu_nav.databinding.ActivityMainBinding
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val json = loadJSONFromAsset("building_data.json")

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.mainLayout)

        binding.btnScanQR.setOnClickListener {
            if (json != null) {
                try {
                    val building = parseJson(json)
                    Log.d("building", building.toString())
                    if (building != null) {
                        val intent = Intent(this, MapActivity::class.java).apply {
                            putExtra("buildingJson", Gson().toJson(building))
                        }
                        startActivity(intent)
                    } else {
                        binding.tvResult.text = "Ошибка парсинга JSON или неполные данные"
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Ошибка: ${e.message}", e)
                    binding.tvResult.text = "Ошибка"
                }
            } else {
                binding.tvResult.text = "Ошибка загрузки JSON файла"
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