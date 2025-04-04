package com.SavenkoProjects.srspu_nav

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
data class JSONData(
    val currentBuilding: BuildingData,
    val currentFloor: Floor,
    val firstFloor: Floor,
    val currentRoom: Door
)
