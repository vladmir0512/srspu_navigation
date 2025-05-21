package com.SavenkoProjects.srspu_nav.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityCampusBinding
import com.SavenkoProjects.srspu_nav.data.Constants.BUILDING_ID
import com.SavenkoProjects.srspu_nav.utils.IntentConstants.EXTRA_BUILDING_ID

class CampusActivity : AppCompatActivity() {

	inner class WebAppInterface(private val context: Context) {
		@JavascriptInterface
		fun showToast(message: String) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
		}

		@JavascriptInterface
		fun openSearchActivity(buildingId: String) {
			val intent = Intent(context, SearchActivity::class.java).apply {
				putExtra(BUILDING_ID, buildingId)
			}
			startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityCampusBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		// Получаем buildingId из Intent
		val buildingId = intent.getStringExtra(EXTRA_BUILDING_ID) ?: "6"
		
		setWebView(binding, buildingId)
	}

	private fun setWebView(binding: ActivityCampusBinding, buildingId: String) {
		val webView = binding.webView
		webView.settings.javaScriptEnabled = true
		webView.addJavascriptInterface(WebAppInterface(this), "Android")
		webView.loadUrl("file:///android_asset/map.html")
		webView.settings.builtInZoomControls = true
		webView.settings.displayZoomControls = true
		
		// Добавляем слушатель загрузки страницы
		webView.webViewClient = object : android.webkit.WebViewClient() {
			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				// Вызываем JavaScript функцию для подсветки корпуса
				webView.evaluateJavascript(
					"javascript:highlightBuilding('$buildingId')",
					null
				)
			}
		}
	}
}