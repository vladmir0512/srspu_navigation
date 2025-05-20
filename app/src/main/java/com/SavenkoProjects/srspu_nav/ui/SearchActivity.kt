package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.data.BuildingConfig
import com.SavenkoProjects.srspu_nav.data.Constants.INPUT_CORRECT_ROOM_NUMBER
import com.SavenkoProjects.srspu_nav.data.Constants.SEARCH_TEXT
import com.SavenkoProjects.srspu_nav.data.RoomManager
import com.SavenkoProjects.srspu_nav.databinding.ActivitySearchBinding
import com.SavenkoProjects.srspu_nav.utils.IntentConstants.EXTRA_BUILDING_ID

class SearchActivity : AppCompatActivity() {
	private lateinit var editTextSearch: AutoCompleteTextView
	private lateinit var searchButton: Button
	private lateinit var mapButton: Button
	private lateinit var roomManager: RoomManager
	private lateinit var currentBuildingId: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivitySearchBinding.inflate(layoutInflater)
		setContentView(binding.root)
		editTextSearch = binding.editTextSearch
		searchButton = binding.searchButton
		mapButton = binding.mapButton
		roomManager = RoomManager()
		
		// Получаем buildingId из Intent
		currentBuildingId = intent.getStringExtra(EXTRA_BUILDING_ID) ?: "6"
		Log.d("SearchActivity", "Received building ID: $currentBuildingId")
		
		setupAutoComplete()
		setOnClickListeners()
	}

	private fun setupAutoComplete() {
		val suggestions = generateRoomSuggestions()
		val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
		editTextSearch.setAdapter(adapter)
		editTextSearch.threshold = 1 // Начинаем показывать подсказки после ввода 1 символа
	}

	private fun generateRoomSuggestions(): List<String> {
		return roomManager.getRoomsForBuilding(currentBuildingId)
			.toList()
			.sortedWith(compareBy<String> { it.length }
				.thenBy { it })
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
						putExtra(EXTRA_BUILDING_ID, currentBuildingId)
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
			val intent = Intent(this, CampusActivity::class.java).apply {
				putExtra(EXTRA_BUILDING_ID, currentBuildingId)
			}
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