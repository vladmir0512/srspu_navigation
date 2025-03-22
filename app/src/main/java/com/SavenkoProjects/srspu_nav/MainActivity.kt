package com.SavenkoProjects.srspu_nav

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.SavenkoProjects.srspu_nav.databinding.ActivityMainBinding
import com.caverock.androidsvg.SVG
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScanQR.setOnClickListener {

            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("buildingId", "0")
                //putExtra("buildingJson", Gson().toJson(building))
            }
            startActivity(intent)
        }
    }



    private fun loadSvgBitmap(svgFileName: String): Bitmap? {
        return try {
            assets.open(svgFileName).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                val picture = svg.renderToPicture()
                val drawable = PictureDrawable(picture)
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки SVG", e)
            null
        }
    }
}