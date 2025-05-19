package com.SavenkoProjects.srspu_nav.ui

import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityCampusBinding

class CampusActivity : AppCompatActivity() {

	inner class WebAppInterface(private val context: Context) {
		@JavascriptInterface
		fun showToast(message: String) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityCampusBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setWebView(binding)
	}

	private fun setWebView(binding: ActivityCampusBinding) {
		val webView = binding.webView
		webView.settings.javaScriptEnabled = true
		webView.addJavascriptInterface(WebAppInterface(this), "Android")
		webView.loadUrl("file:///android_asset/map.html")
		webView.settings.builtInZoomControls = true
		webView.settings.displayZoomControls = true


	}

}