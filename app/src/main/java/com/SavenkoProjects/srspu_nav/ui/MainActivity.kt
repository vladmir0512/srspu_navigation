package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityMainBinding
import com.SavenkoProjects.srspu_nav.utils.IntentConstants.EXTRA_BUILDING_ID
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	private val qrCodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
		if (scanResult != null) {
			if (scanResult.contents != null) {
				val scannedValue = scanResult.contents
				Log.d("QR_SCAN", "Получено из QR: '$scannedValue'")
				Log.d("QR_SCAN", "Длина строки: ${scannedValue.length}")
				Log.d("QR_SCAN", "Коды символов: ${scannedValue.map { it.code }}")
				
				if (scannedValue.matches(Regex("^[1-8]$"))) {
					// Преобразуем значение: 1->0, 2->1, 3->2, и т.д.
					val buildingId = (scannedValue.toInt() - 1).toString()
					Log.d("QR_SCAN", "Преобразованное значение: '$buildingId'")
					
					val intentSearchActivity = Intent(this, SearchActivity::class.java).apply {
						putExtra(EXTRA_BUILDING_ID, buildingId)
					}
					startActivity(intentSearchActivity)
				} else {
					Toast.makeText(this, "Неверный формат номера здания", Toast.LENGTH_SHORT).show()
					Log.e("QR_SCAN", "Неверный формат. Получено: '$scannedValue'")
				}
			} else {
				Toast.makeText(this, "QR-код не распознан", Toast.LENGTH_SHORT).show()
				Log.e("QR_SCAN", "QR-код не распознан")
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		binding.btnScanQR.setOnClickListener {
			val integrator = IntentIntegrator(this)
			integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
			integrator.setPrompt("Наведите камеру на QR-код с номером здания (1-8)")
			integrator.setCameraId(0)
			integrator.setBeepEnabled(false)
			integrator.setBarcodeImageEnabled(false)
			integrator.initiateScan()
		}
	}

	@Deprecated("This method has been deprecated in favor of using the Activity Result API")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
		if (result != null) {
			if (result.contents != null) {
				val scannedValue = result.contents
				Log.d("QR_SCAN", "Получено из QR: '$scannedValue'")
				Log.d("QR_SCAN", "Длина строки: ${scannedValue.length}")
				Log.d("QR_SCAN", "Коды символов: ${scannedValue.map { it.code }}")
				
				if (scannedValue.matches(Regex("^[1-8]$"))) {
					// Преобразуем значение: 1->0, 2->1, 3->2, и т.д.
					val buildingId = (scannedValue.toInt() - 1).toString()
					Log.d("QR_SCAN", "Преобразованное значение: '$buildingId'")
					
					val intentSearchActivity = Intent(this, SearchActivity::class.java).apply {
						putExtra(EXTRA_BUILDING_ID, buildingId)
					}
					startActivity(intentSearchActivity)
				} else {
					Toast.makeText(this, "Неверный формат номера здания", Toast.LENGTH_SHORT).show()
					Log.e("QR_SCAN", "Неверный формат. Получено: '$scannedValue'")
				}
			} else {
				Toast.makeText(this, "QR-код не распознан", Toast.LENGTH_SHORT).show()
				Log.e("QR_SCAN", "QR-код не распознан")
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data)
		}
	}
}