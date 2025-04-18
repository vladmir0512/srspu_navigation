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
    const val START_POSITION = "start_position"
    const val SEARCH_TEXT = "searchText"
    const val LEFT = "left"
    const val STAIR = "stair"
    const val STAIRS = "stairs"
    const val HSL = "HSL"
    const val HSR = "HSR"
    const val STAIRS_LEFT = "left"
    const val STAIRS_RIGHT = "left"
    const val ROTATION_X = "rotationX"
    const val IMAGE_CAMPUS = "file:///android_asset/karta.jpg"
    const val MAIN_ACTIVITY = "MainActivity"
    const val ROUTE_MANAGER = "RouteManager"
    const val PATH_FINDER = "PathFinder"
    const val STAIRCASE_FINDER = "StairCase"
    const val ROUTES_ACTIVITY = "Непредвиденная ошибка при парсинге json: "
    const val INCORRECT_BUILDING_NUMBER = "Неверный номер здания"
    const val NOT_AVAILABLE_DRAW_PATH = "Не удалось нарисовать путь на этаже"
    const val NOT_FOUND_ROUTE = "Маршрут не найден"
    const val NOT_FOUND_ROUTE_FIRST_FLOOR = "Маршрут на 1 этаже не найден"
    const val EXCEPTION_LOAD_MAP = "Ошибка загрузки карты этажа: "
    const val EXCEPTION_LOAD_JSON = "Ошибка загрузки JSON: "
    const val ITERATIONS_LIMIT_EXCEEDED = "Превышено максимальное количество итераций при поиске пути"
    const val NOT_FOUND_START_OR_END_POINT = "Начальная или конечная точка не существует в данных этажа "
    const val ERROR_JSON = "Ошибка парсинга JSON: "
    const val UNEXPECTED_ERROR_JSON = "Непредвиденная ошибка при парсинге JSON: "
    const val INPUT_CORRECT_ROOM_NUMBER = "Непредвиденная ошибка при парсинге JSON: "
}