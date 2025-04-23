package com.SavenkoProjects.srspu_nav.ui

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.data.Building
import com.SavenkoProjects.srspu_nav.data.Constants.EXCEPTION_LOAD_JSON
import com.SavenkoProjects.srspu_nav.data.Constants.JSON_READER
import com.SavenkoProjects.srspu_nav.data.Constants.SEARCH_TEXT
import com.SavenkoProjects.srspu_nav.data.JsonReader
import com.SavenkoProjects.srspu_nav.data.SvgReader
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding
import com.SavenkoProjects.srspu_nav.managers.MapManager
import com.SavenkoProjects.srspu_nav.utils.AnimationManager
import java.io.IOException


class RoutesActivity : AppCompatActivity() {
	private lateinit var binding: ActivityRoutesBinding
	private lateinit var mapManager: MapManager
	private lateinit var animationManager: AnimationManager
	private lateinit var jsonReader: JsonReader
	private lateinit var svgReader: SvgReader

	private var isSearchVisible = false
	private var isFirstState = true
	private var buildingId = 1
	private var building: Building? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val json = loadJSONFromAsset().toString()

		binding = ActivityRoutesBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initializeManagers()
		setupUI()
		loadMaps(json)

	}

	private fun loadMaps(json: String) {
		binding.mapImageView.layoutParams as FrameLayout.LayoutParams
		val paramsMainImageView =
			binding.floorMapImageView.layoutParams as FrameLayout.LayoutParams
		val searchText = intent.getStringExtra(SEARCH_TEXT).toString()
		val floor = searchText[0].toString().toInt()
		mapManager.resizeImageView(floor, paramsMainImageView)
		building = jsonReader.parseJson(json)
		if (building != null) {
			mapManager.drawMapWithRoute(
				buildingId = buildingId,
				building = building!!,
				endRoomId = searchText
			)
		}
	}

	private fun initializeManagers() {
		animationManager = AnimationManager(binding)
		svgReader = SvgReader(this)
		mapManager = MapManager(this, binding, buildingId)
		jsonReader = JsonReader()
	}

	fun loadJSONFromAsset(): String? {
		return try {
			val filename: String = "building_data.json"
			assets.open(filename).bufferedReader().use { it.readText() }
		} catch (ex: IOException) {
			Log.e(JSON_READER, EXCEPTION_LOAD_JSON + ex.message)
			null
		}
	}

	private fun setupUI() {
		binding.searchButton.setOnClickListener {
			animationManager.toggleSearchFieldAnimation(isSearchVisible)
			isSearchVisible = !isSearchVisible
		}

		binding.floorMapImageViewBack.setOnClickListener {
			animationManager.rotateMapAnimation(binding.floorMapImageViewBack, isFirstState)
			isFirstState = !isFirstState
		}
	}
}