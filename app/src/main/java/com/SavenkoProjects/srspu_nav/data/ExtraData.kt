package com.SavenkoProjects.srspu_nav.data

//ExtraData.kt
data class Building(
    val building: List<BuildingData>
)

data class BuildingData(
    val id: Int,
    val name: String,
    val floors: List<Floor>
)

data class Floor(
    val id: Int,
    val startPosition: List<Int>,
    val doors: Map<String, Door>,
    val hallways: Map<String, Hallway>,
    val connections: Map<String, List<String>>
)

data class Door(
    val position: List<Int>
)

data class Hallway(
    val path: List<List<Int>>
)

object Constants {
    const val BUILDING_ID = "building_id"
    const val FLOOR_NUMBER = "floor_number"
    const val ROOM_NUMBER = "room_number"
    const val START_POSITION = "start_position"
    const val END_POSITION = "end_position"
}