package com.SavenkoProjects.srspu_nav.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import com.SavenkoProjects.srspu_nav.data.BuildingData
import com.SavenkoProjects.srspu_nav.data.Floor
import com.SavenkoProjects.srspu_nav.data.SvgReader
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding
import com.SavenkoProjects.srspu_nav.drawing.PathDrawer
import com.SavenkoProjects.srspu_nav.navigation.PathFinder
import com.SavenkoProjects.srspu_nav.navigation.StaircaseFinder
import java.io.IOException

data class RouteManager(
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
            val pathIds = pathFinder.findPath(firstFloor, "startPosition", endRoomId)
            val pathPoints = pathIds.mapNotNull {
                pathDrawer.getPoint(
                    it, firstFloor.doors, firstFloor.hallways,
                    firstFloor.startPosition
                )
            }
            if (pathPoints.isNotEmpty()) {
                pathDrawer.drawPath(firstFloorCanvas, pathPoints)
                binding.floorMapImageView.setImageBitmap(firstFloorBitmap)
                Toast.makeText(
                    context, "Маршрут на 1 этаже не найден",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val targetFloor = currentBuilding.floors.find { it.id == floorNumber } ?: return
            val endRoom = targetFloor.doors[endRoomId] ?: return
            val targetStaircase = staircaseFinder.findNearestStaircase(endRoom.position[0], targetFloor)
            val firstFloorStaircase =
                if (targetStaircase.contains("left")) "stairs_left" else "stairs_right"
            val firstFloorPath = pathFinder.findPath(
                firstFloor, "startPosition",
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
                    drawHigherFloorRoute(buildingTag, targetFloor, targetStaircase, endRoomId)
                } else {
                    Toast.makeText(
                        context,
                        "Маршрут на первом этаже не найден",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("MapActivity", "buildingTag не найден")
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
                Toast.makeText(context, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Аудитория $endRoomId отсутствует на этаже ${floor.id}")
                return
            }
            val pathIds = pathFinder.findPath(floor, staircase, endRoomId)
            if (pathIds.isEmpty()) {
                Toast.makeText(context, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e(
                    "MapActivity",
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
                Toast.makeText(context, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                Log.e("MapActivity", "Не удалось нарисовать путь на этаже ${floor.id}")
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Ошибка загрузки карты этажа", e)
        }
    }
}