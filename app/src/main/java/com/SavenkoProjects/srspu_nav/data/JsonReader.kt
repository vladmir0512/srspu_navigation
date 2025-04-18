package com.SavenkoProjects.srspu_nav.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class JsonReader {

	fun parseJson(json: String): Building? {
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