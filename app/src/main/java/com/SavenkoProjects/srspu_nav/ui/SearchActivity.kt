package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.data.Constants.BUILDING_ID
import com.SavenkoProjects.srspu_nav.data.Constants.INPUT_CORRECT_ROOM_NUMBER
import com.SavenkoProjects.srspu_nav.data.Constants.SEARCH_TEXT
import com.SavenkoProjects.srspu_nav.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
	private lateinit var editTextSearch: EditText
	private lateinit var searchButton: Button
	private lateinit var mapButton: Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivitySearchBinding.inflate(layoutInflater)
		setContentView(binding.root)
		editTextSearch = binding.editTextSearch
		searchButton = binding.searchButton
		mapButton = binding.mapButton
		setOnClickListeners()
	}

	private fun setOnClickListeners() {
		searchButton.setOnClickListener {
			val searchText = editTextSearch.text.toString()
			val isValid = validateSearchText(searchText)

			if (isValid) {
				val intent = Intent(this, RoutesActivity::class.java).apply {
					putExtra(SEARCH_TEXT, searchText)
				}
				startActivity(intent)
			} else {
				editTextSearch.error = INPUT_CORRECT_ROOM_NUMBER
			}

			mapButton.setOnClickListener {
				val buildingId = intent.getStringExtra(BUILDING_ID)
				val intent = Intent(this, CampusActivity::class.java).apply {
					putExtra(BUILDING_ID, buildingId)
				}

				startActivity(intent)
			}
		}
	}

	/** Проверяем с помощью [Regex], что [searchText] содержит ровно 3 цифры в начале, может содержать только символ 'А'  */
	private fun validateSearchText(searchText: String): Boolean {
		val regex = Regex("^\\d{3}[АБВГК]?$")
		return searchText.matches(regex)
	}

}