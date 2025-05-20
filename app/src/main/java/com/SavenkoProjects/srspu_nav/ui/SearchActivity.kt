package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.data.Constants.INPUT_CORRECT_ROOM_NUMBER
import com.SavenkoProjects.srspu_nav.data.Constants.SEARCH_TEXT
import com.SavenkoProjects.srspu_nav.data.Constants.BUILDING_ID
import com.SavenkoProjects.srspu_nav.databinding.ActivitySearchBinding
import com.SavenkoProjects.srspu_nav.data.RoomManager
import com.SavenkoProjects.srspu_nav.data.BuildingConfig

class SearchActivity : AppCompatActivity() {
	private lateinit var editTextSearch: EditText
	private lateinit var searchButton: Button
	private lateinit var mapButton: Button
	private lateinit var roomManager: RoomManager
	private var currentBuildingId: String = "0" // По умолчанию ЛК

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivitySearchBinding.inflate(layoutInflater)
		setContentView(binding.root)
		editTextSearch = binding.editTextSearch
		searchButton = binding.searchButton
		mapButton = binding.mapButton
		roomManager = RoomManager()
		
		// Получаем buildingId из Intent, если он был передан
		val receivedBuildingId = intent.getStringExtra(BUILDING_ID)
		Log.d("SearchActivity", "Received building ID: $receivedBuildingId")
		
		// Проверяем, что buildingId валидный (от 0 до 7)
		currentBuildingId = if (receivedBuildingId != null && receivedBuildingId in "0".."7") {
			receivedBuildingId
		} else {
			Log.w("SearchActivity", "Invalid building ID received: $receivedBuildingId, using default: 0")
			"0"
		}
		
		Log.d("SearchActivity", "Current building ID: $currentBuildingId (${BuildingConfig.BUILDINGS[currentBuildingId.toInt()]})")
		setOnClickListeners()
	}

	private fun setOnClickListeners() {
		searchButton.setOnClickListener {
			val searchText = editTextSearch.text.toString()
			val isValid = validateSearchText(searchText)
			Log.d("SearchActivity", "Validating room number: $searchText, isValid: $isValid")

			if (isValid) {
				if (roomManager.isRoomExists(searchText, currentBuildingId)) {
					val intent = Intent(this, RoutesActivity::class.java).apply {
						putExtra(SEARCH_TEXT, searchText)
						putExtra(BUILDING_ID, currentBuildingId)
					}
					startActivity(intent)
				} else {
					Toast.makeText(this, "Аудитория $searchText не найдена в выбранном здании", Toast.LENGTH_LONG).show()
				}
			} else {
				editTextSearch.error = INPUT_CORRECT_ROOM_NUMBER
			}
		}
		mapButton.setOnClickListener {
			val intent = Intent(this, CampusActivity::class.java)
			startActivity(intent)
		}
	}

	/** Проверяем с помощью [Regex], что [searchText] содержит ровно 3 цифры в начале, может содержать только символ 'А'  */
	private fun validateSearchText(searchText: String): Boolean {
		val regex = Regex("^\\d{3}[АБВГК]?$")
		val matches = searchText.matches(regex)
		Log.d("SearchActivity", "Regex validation for $searchText: $matches")
		return matches
	}
}