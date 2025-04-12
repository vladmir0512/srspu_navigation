package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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
                    putExtra("searchText", searchText)
                }
                startActivity(intent)
            } else {
                editTextSearch.error = "Введите корректный номер аудитории"
            }

            mapButton.setOnClickListener {
                val buildingId = intent.getStringExtra("buildingId")
                val intent = Intent(this, CampusActivity::class.java).apply {
                    putExtra("buildingId", buildingId)
                }

                startActivity(intent)
            }
        }
    }

    /** Проверяем с помощью [Regex], что [searchText] содержит ровно 3 цифры в начале, может содержать только символ 'А'  */
    private fun validateSearchText(searchText: String): Boolean {
        val regex = Regex("^\\d{3}[А,Б]?$")
        return searchText.matches(regex)
    }

}




