package com.SavenkoProjects.srspu_nav.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.SVG

class SvgReader(
	private val context: Context
) {
	fun loadSvgToBitmap(svgFileName: String): Bitmap {
		return renderSvgToBitmap(loadSvgFromAssets(svgFileName))
	}

	private fun loadSvgFromAssets(svgFileName: String): SVG {
		val svg = SVG.getFromAsset(context.assets, svgFileName)
		return svg
	}

	private fun renderSvgToBitmap(svg: SVG): Bitmap {
		val bitmap = createBitmap(svg.documentWidth.toInt(), svg.documentHeight.toInt())
		val canvas = Canvas(bitmap)
		svg.renderToCanvas(canvas)
		return bitmap
	}
}