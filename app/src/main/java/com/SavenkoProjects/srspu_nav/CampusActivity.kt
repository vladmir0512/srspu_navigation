package com.SavenkoProjects.srspu_nav

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.data.Constants.IMAGE_CAMPUS
import com.SavenkoProjects.srspu_nav.databinding.ActivityCampusBinding


class CampusActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityCampusBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setWebView(binding)
	}

	private fun setWebView(binding: ActivityCampusBinding) {
		val webView = binding.webView
		webView.loadUrl(IMAGE_CAMPUS)
		webView.settings.builtInZoomControls = true
		webView.settings.displayZoomControls = true
	}
}