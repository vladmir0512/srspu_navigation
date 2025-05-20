package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityMainBinding
import com.SavenkoProjects.srspu_nav.utils.IntentConstants.EXTRA_BUILDING_ID

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		binding.btnScanQR.setOnClickListener {
			val intentSearchActivity = Intent(this, SearchActivity::class.java).apply {
				putExtra(EXTRA_BUILDING_ID, "0")
				//putExtra("buildingJson", Gson().toJson(building)) TODO сделать переход в поиск
			}
			startActivity(intentSearchActivity)
		}
	}


}