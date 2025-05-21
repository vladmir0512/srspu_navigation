package com.SavenkoProjects.srspu_nav.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityMainBinding
import com.SavenkoProjects.srspu_nav.utils.CryptoUtils
import com.SavenkoProjects.srspu_nav.utils.IntentConstants.EXTRA_BUILDING_ID
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	private val qrCodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
		if (scanResult != null) {
			if (scanResult.contents != null) {
				try {
					val decryptedValue = CryptoUtils.decrypt(scanResult.contents)
					Log.d("QR_SCAN", "Расшифровано из QR: '$decryptedValue'")
					Log.d("QR_SCAN", "Длина строки: ${decryptedValue.length}")
					Log.d("QR_SCAN", "Коды символов: ${decryptedValue.map { it.code }}")
					
					if (decryptedValue.matches(Regex("^[0-7]$"))) {
						val intentSearchActivity = Intent(this, SearchActivity::class.java).apply {
							putExtra(EXTRA_BUILDING_ID, decryptedValue)
						}
						startActivity(intentSearchActivity)
					} else {
						Toast.makeText(this, "Неверный формат номера здания", Toast.LENGTH_SHORT).show()
						Log.e("QR_SCAN", "Неверный формат. Расшифровано: '$decryptedValue'")
					}
				} catch (e: Exception) {
					Toast.makeText(this, "Ошибка при расшифровке QR-кода", Toast.LENGTH_SHORT).show()
					Log.e("QR_SCAN", "Ошибка расшифровки: ${e.message}")
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
		
		// Тестируем шифрование чисел
		CryptoUtils.testEncryption()
		
		binding.btnScanQR.setOnClickListener {
			val integrator = IntentIntegrator(this)
			integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
			integrator.setPrompt("Наведите камеру на QR-код с номером здания (0-7)")
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
				try {
					val decryptedValue = CryptoUtils.decrypt(result.contents)
					Log.d("QR_SCAN", "Расшифровано из QR: '$decryptedValue'")
					Log.d("QR_SCAN", "Длина строки: ${decryptedValue.length}")
					Log.d("QR_SCAN", "Коды символов: ${decryptedValue.map { it.code }}")
					
					if (decryptedValue.matches(Regex("^[0-7]$"))) {
						val intentSearchActivity = Intent(this, SearchActivity::class.java).apply {
							putExtra(EXTRA_BUILDING_ID, decryptedValue)
						}
						startActivity(intentSearchActivity)
					} else {
						Toast.makeText(this, "Неверный формат номера здания", Toast.LENGTH_SHORT).show()
						Log.e("QR_SCAN", "Неверный формат. Расшифровано: '$decryptedValue'")
					}
				} catch (e: Exception) {
					Toast.makeText(this, "Ошибка при расшифровке QR-кода", Toast.LENGTH_SHORT).show()
					Log.e("QR_SCAN", "Ошибка расшифровки: ${e.message}")
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