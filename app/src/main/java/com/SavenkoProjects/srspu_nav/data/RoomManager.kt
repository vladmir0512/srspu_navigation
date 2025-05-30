package com.SavenkoProjects.srspu_nav.data

import android.util.Log

class RoomManager {
    // Список существующих аудиторий по зданиям
    private val existingRooms = mutableMapOf(
        "0" to setOf( // ЛК (ЛАБОРАТОРНЫЙ КОРПУС)
            "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "119", "121", "123", "117А",
            "202", "204", "205", "206", "207", "208", "209", "210", "211", "212", "213", "214", "216", "217", "218", "219", "220", "221", "222", "223", "225", "207А", "210А", "202А",
            "301", "302", "304", "305", "306", "307", "308", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321", "322", "323", "313А", "308А",
            "401", "402", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414", "415", "416", "417", "418", "419", "420", "421", "422", "423", "424", "426", "428", "407А", "410А"
        ),
        "1" to setOf( // Главный корпус (передняя часть)
            "101", "107", "110", "112", "114", "117", "118", "119", "120", "123", "124", "125", "130", "132", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "151", "126А", "148А",
            "203", "204", "205", "207", "208", "209", "210", "210А", "211", "211А", "212", "213", "214", "215", "216", "216А", "217", "218", "220", "221", "222", "222А", "222Б", "223", "225", "225А", "226", "228", "229", "230", "231", "232", "233", "235", "236", "237", "238", "239", "240", "241", "242", "243",
            "304", "304А", "305", "306", "307", "309", "310", "310А", "311", "312", "314", "314А", "315", "316", "317", "317А", "321", "321А", "322", "323", "324", "325", "325А", "326", "326А", "326Б", "326В", "326Г", "326К", "331", "332", "333", "334", "334А", "336",
            "403", "404", "405", "406", "407", "408", "409", "410", "411", "412"
        ),
        "2" to setOf( // Главный корпус (задняя часть)
            "101", "107", "110", "112", "114", "117", "118", "119", "120", "123", "124", "125", "130", "132", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "151", "126А", "148А",
            "203", "204", "205", "207", "208", "209", "210", "210А", "211", "211А", "212", "213", "214", "215", "216", "216А", "217", "218", "220", "221", "222", "222А", "222Б", "223", "225", "225А", "226", "228", "229", "230", "231", "232", "233", "235", "236", "237", "238", "239", "240", "241", "242", "243",
            "304", "304А", "305", "306", "307", "309", "310", "310А", "311", "312", "314", "314А", "315", "316", "317", "317А", "321", "321А", "322", "323", "324", "325", "325А", "326", "326А", "326Б", "326В", "326Г", "326К", "331", "332", "333", "334", "334А", "336",
            "403", "404", "405", "406", "407", "408", "409", "410", "411", "412"
        ),
        "3" to setOf( // РТ
            "101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
            "111", "112", "113", "114", "115", "116", "117", "118", "119", "120",
            "121", "122", "123",
            "201", "202", "203", "204", "205", "206", "207", "208", "209", "210",
            "211", "212", "213", "214", "215", "216", "217", "218", "219", "220",
            "221", "222", "223", "224", "225", "226", "227",
            "301", "302", "303", "304", "305", "306", "307", "308", "309", "310",
            "311", "312", "313", "314", "315", "316", "317", "318", "319", "320",
            "321", "322", "323", "324", "325", "326",
            "401", "402", "403", "404", "405", "406", "407", "408", "409", "410",
            "411", "412", "413", "414", "415", "416", "417", "418", "419", "420",
            "421", "422", "424", "425", "426", "427", "428"
        ),
        "4" to setOf( // НРГ
            "101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
            "201", "202", "203", "204", "205", "206", "207", "208", "209", "210"
             ),
        "5" to setOf( // УБК
            "101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
            "201", "202", "203", "204", "205", "206", "207", "208", "209", "210",
            "301", "302", "303", "304", "305", "306", "307", "308", "309", "310",
            "401", "402", "403", "404", "405", "406", "407", "408", "409", "410",
            "501", "502", "503", "504", "505", "506", "507", "508", "509", "510"
       
            ),
        "6" to setOf( // ГГ
            "101", "102", "103", "104", "105", "106", "106А", "107", "108", "109", "115", "116", "117", "118", "119", "120", "121", "122",
            "201", "202", "203", "204", "205", "205А", "206", "207", "208", "209", "210", "211", "212", "212А", "213", "214", "215", "216", "217", "218", "219", "220", "221", "222", "223", "224", "225", "226", "227",
            "301", "302", "303", "304", "305", "306", "306А", "307", "308", "309", "310", "312", "313", "314", "315", "315А", "316", "317", "318",
            "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414", "415", "416", "417", "418", "419", "420"
        ),
        "7" to setOf( // Хим
            "101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
            "201", "202", "203", "204", "205", "206", "207", "208", "209", "210",
            "301", "302", "303", "304", "305", "306", "307", "308", "309", "310",
            "401", "402", "403", "404", "405", "406", "407", "408", "409", "410",
            "501", "502", "503", "504", "505", "506", "507", "508", "509", "510"
        )
    )

    /**
     * Проверяет существование аудитории в указанном здании
     * @param roomNumber номер аудитории для проверки
     * @param buildingId идентификатор здания
     * @return true если аудитория существует в указанном здании, false в противном случае
     */
    fun isRoomExists(roomNumber: String, buildingId: String): Boolean {
        val exists = existingRooms[buildingId]?.contains(roomNumber) ?: false
        Log.d("RoomManager", "Checking room $roomNumber in building $buildingId: $exists")
        Log.d("RoomManager", "Available rooms in building $buildingId: ${existingRooms[buildingId]?.joinToString()}")
        return exists
    }

    /**
     * Получает список всех аудиторий для указанного здания
     * @param buildingId идентификатор здания
     * @return Set с номерами аудиторий или пустой Set, если здание не найдено
     */
    fun getRoomsForBuilding(buildingId: String): Set<String> {
        return existingRooms[buildingId] ?: emptySet()
    }
} 