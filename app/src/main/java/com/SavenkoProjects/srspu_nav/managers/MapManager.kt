package com.SavenkoProjects.srspu_nav.managers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.SavenkoProjects.srspu_nav.data.Building
import com.SavenkoProjects.srspu_nav.data.BuildingConfig
import com.SavenkoProjects.srspu_nav.data.Constants.INCORRECT_BUILDING_NUMBER
import com.SavenkoProjects.srspu_nav.data.Constants.ROUTES_ACTIVITY
import com.SavenkoProjects.srspu_nav.data.SvgReader
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding

class MapManager(
	private val context: Context,
	private val binding: ActivityRoutesBinding,
	private var buildingId: Int
) {
	private val svgManager: SvgReader = SvgReader(context)
	private val routeManager: RouteManager = RouteManager(context, binding)

	@SuppressLint("ShowToast")
	fun resizeImageView(floor: Int, paramsMainImageView: FrameLayout.LayoutParams) {
		when (buildingId) {
			0 -> {
				binding.floorMapImageView.scaleType = ImageView.ScaleType.CENTER_CROP
				binding.mapImageView.scaleType = ImageView.ScaleType.CENTER_CROP
				if (floor > 1) {
					binding.floorMapImageView.layoutParams = paramsMainImageView
				}
			}

			1, 2, 3, 5, 6, 7 -> {
				binding.floorMapImageView.scaleType = ImageView.ScaleType.FIT_CENTER
				binding.mapImageView.scaleType = ImageView.ScaleType.FIT_CENTER
			}

			4 -> {
				binding.floorMapImageView.setPadding(0, 10, 0, 10)
				binding.mapImageView.setPadding(0, 10, 0, 10)
				binding.floorMapImageView.scaleType = ImageView.ScaleType.FIT_CENTER
				binding.mapImageView.scaleType = ImageView.ScaleType.FIT_CENTER
			}

			else -> {
				Toast.makeText(
					context, "Неверный номер здания",
					Toast.LENGTH_SHORT
				)
				Log.e(ROUTES_ACTIVITY, INCORRECT_BUILDING_NUMBER)
			}
		}
	}

	fun drawMapWithRoute(buildingId: Int, endRoomId: String, building: Building) {
		val floor = endRoomId.firstOrNull()?.digitToIntOrNull()
		val buildingsDict = BuildingConfig.BUILDINGS
		val buildings = building.building
		val buildingTag = buildingsDict[buildingId]
		val currentBuilding = buildings[buildingId]
		val firstFloor = currentBuilding.floors.find { it.id == 1 } ?: return
		val firstFloorBitmap: Bitmap =
			svgManager.loadSvgToBitmap("maps/${buildingTag}_1.svg")
		val firstFloorCanvas = Canvas(firstFloorBitmap)
		Log.d("MapManager", "Тег выбранного здания $buildingTag")
		Toast.makeText(context, "Здание ${currentBuilding.name} выбрано", Toast.LENGTH_SHORT).show()
		when (floor) {
			1, 2, 3, 4 -> routeManager.drawRouteOnHigherFloor(
				currentBuilding,
				floor,
				endRoomId,
				firstFloor,
				firstFloorCanvas,
				firstFloorBitmap,
				buildingTag
			)

			else -> {
				Log.e(
					"MapManager",
					"Неопределенное значение floor: Int для drawMapWithRoute().\n " +
							"Ожидается от 1 до 4."
				)
			}
		}

	}

	@SuppressLint("UseKtx")
	private fun Int.toPx(context: Context): Int =
		(this * context.resources.displayMetrics.density).toInt()
}