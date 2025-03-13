package com.SavenkoProjects.srspu_nav
//ExtraData.kt
data class Building(
    val building: BuildingData
)

data class BuildingData(
    val name: String,
    val doors: List<Door>,
    val hallways: List<Hallway>,
    val connections: List<Connection>
)

data class Door(
    val id: Any,
    val x: Int,
    val y: Int
)

data class Hallway(
    val id: String,
    val x: Int,
    val y: Int
)

data class Connection(
    val from: String,
    val to: String
)