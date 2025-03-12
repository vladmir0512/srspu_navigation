package com.SavenkoProjects.srspu_nav

data class Building(
    val building: BuildingData
)

data class BuildingData(
    val name: String,
    val doors: List<Door>
)

data class Door(
    val id: Any,
    val x: Int,
    val y: Int
)