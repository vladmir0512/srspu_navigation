package com.SavenkoProjects.srspu_nav.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.SavenkoProjects.srspu_nav.data.BuildingData
import com.SavenkoProjects.srspu_nav.data.Constants.EXCEPTION_LOAD_MAP
import com.SavenkoProjects.srspu_nav.data.Constants.NOT_AVAILABLE_DRAW_PATH
import com.SavenkoProjects.srspu_nav.data.Constants.ROUTE_MANAGER
import com.SavenkoProjects.srspu_nav.data.Constants.START_POSITION
import com.SavenkoProjects.srspu_nav.data.Floor
import com.SavenkoProjects.srspu_nav.data.SvgReader
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding
import com.SavenkoProjects.srspu_nav.drawing.PathDrawer
import com.SavenkoProjects.srspu_nav.navigation.PathFinder
import com.SavenkoProjects.srspu_nav.navigation.StaircaseFinder
import java.io.IOException

class RouteManager(
	private val context: Context,
	private val binding: ActivityRoutesBinding,
) {
	private val pathFinder = PathFinder()
	private val pathDrawer = PathDrawer()
	private val staircaseFinder = StaircaseFinder()

	fun drawRouteOnHigherFloor(
		currentBuilding: BuildingData,
		floorNumber: Int,
		endRoomId: String,
		firstFloor: Floor,
		firstFloorCanvas: Canvas,
		firstFloorBitmap: Bitmap,
		buildingTag: String?
	) {
		if (floorNumber == 1) {
			val pathIds = pathFinder.findPath(firstFloor, START_POSITION, endRoomId)
			val pathPoints = pathIds.mapNotNull {
				pathDrawer.getPoint(
					it, firstFloor.doors, firstFloor.hallways,
					firstFloor.startPosition
				)
			}
			if (pathPoints.isNotEmpty()) {
				pathDrawer.drawPath(firstFloorCanvas, pathPoints)
				binding.floorMapImageView.setImageBitmap(firstFloorBitmap)

			}
		} else {
			val targetFloor = currentBuilding.floors.find { it.id == floorNumber } ?: return
			val endRoom = targetFloor.doors[endRoomId] ?: return

			// Находим ближайшую лестницу на целевом этаже
			val targetStaircase = staircaseFinder.findNearestStaircase(
				endRoom.position[0],
				endRoom.position[1],
				targetFloor
			)

			// Находим соответствующую лестницу на первом этаже
			val firstFloorStaircase = staircaseFinder.findNearestStaircase(
				firstFloor.hallways[targetStaircase]?.path?.get(0)?.get(0) ?: 0,
				firstFloor.hallways[targetStaircase]?.path?.get(0)?.get(1) ?: 0,
				firstFloor
			)

			// Сначала строим путь от стартовой позиции до лестницы на первом этаже
			val firstFloorPath = pathFinder.findPath(
				firstFloor, START_POSITION,
				firstFloorStaircase
			)
			val firstFloorPathPoints = firstFloorPath.mapNotNull {
				pathDrawer.getPoint(
					it, firstFloor.doors, firstFloor.hallways,
					firstFloor.startPosition
				)
			}

			if (firstFloorPathPoints.isNotEmpty()) {
				if (buildingTag != null) {
					pathDrawer.drawPath(firstFloorCanvas, firstFloorPathPoints)
					binding.mapImageView.setImageBitmap(firstFloorBitmap)
					// Затем строим путь от лестницы до целевой аудитории на целевом этаже
					drawHigherFloorRoute(buildingTag, targetFloor, targetStaircase, endRoomId)
				} else {
					Log.d(ROUTE_MANAGER, "buildingTag не найден")
				}
			}
		}
	}

	private fun drawHigherFloorRoute(
		buildingTag: String,
		floor: Floor,
		staircase: String,
		endRoomId: String
	) {
		try {
			val svgReader = SvgReader(context)
			val svgFileName = "maps/${buildingTag}_${floor.id}.svg"
			val floorBitmap = svgReader.loadSvgToBitmap(svgFileName)
			val floorCanvas = Canvas(floorBitmap)

			if (!floor.doors.containsKey(endRoomId)) {
				Log.e(ROUTE_MANAGER, "Аудитория $endRoomId отсутствует на этаже ${floor.id}")
				return
			}

			// Строим путь от лестницы до целевой аудитории
			val pathIds = pathFinder.findPath(floor, staircase, endRoomId)
			if (pathIds.isEmpty()) {
				Log.e(
					ROUTE_MANAGER,
					"Путь не найден на этаже ${floor.id} от $staircase до $endRoomId"
				)
				return
			}
			val pathPoints = pathIds.mapNotNull {
				pathDrawer.getPoint(it, floor.doors, floor.hallways, floor.startPosition)
			}
			if (pathPoints.isNotEmpty()) {
				pathDrawer.drawPath(floorCanvas, pathPoints)
				binding.floorMapImageView.setImageBitmap(floorBitmap)
			} else {
				Log.e(ROUTE_MANAGER, NOT_AVAILABLE_DRAW_PATH + floor.id)
			}
		} catch (e: IOException) {
			Log.e(ROUTE_MANAGER, EXCEPTION_LOAD_MAP, e)
		}
	}
}