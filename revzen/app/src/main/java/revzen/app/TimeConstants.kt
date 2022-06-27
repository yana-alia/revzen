package revzen.app

import revzen.app.api.ApiHandler
import kotlin.math.max
import kotlin.math.roundToInt

const val MINS_TO_MILLIS = 60000
const val SECS_TO_MILLIS = 1000
const val SECS_TO_HOURS = 3600

const val STUDY_PET_THRESHOLD = 30

fun timeFormat(time: Int): String {
    val hours = time / 3600
    val mins = (time % 3600) / 60
    val secs = (time % 3600) % 60

    var out: String = ""
    if(hours != 0) {
        out += "$hours hour"
        out += if(hours == 1){
            " "
        }else{
            "s "
        }
    }
    if(mins != 0) {
        out += "$mins minute"
        out += if(mins == 1){
            " "
        }else{
            "s "
        }
    }
    if(secs != 0) {
        out += "$secs second"
        out += if(secs == 1){
            " "
        }else{
            "s "
        }
    }

    return out
}


fun calculateResult(session_data: ArrayList<SessionData>) : ApiHandler.StudyResult {
    var totalStudyTime = 0
    var totalBreakTime = 0
    var totalPlannedStudyTime = 0
    var totalPlannedBreakTime = 0

    var xpGained = 0
    var healthChange = 0

    for (session in session_data) {
        totalStudyTime += session.study_time
        totalBreakTime += session.break_time
        totalPlannedStudyTime += session.planned_study_time
        totalPlannedBreakTime += session.planned_break_time

        if (session.study_time > session.planned_study_time) {
            xpGained += getXp(totalStudyTime, totalPlannedStudyTime)
        } else {
            healthChange = -1
        }
    }

    return ApiHandler.StudyResult(
        totalStudyTime,
        totalBreakTime,
        totalPlannedStudyTime,
        totalPlannedBreakTime,
        xpGained,
        healthChange
    )
}

fun getXp(actualTime: Int, setTime: Int): Int{
    println(actualTime)
    println(setTime)
    //values in seconds
    var xp: Int = 0
    val maxXp = setTime / 10

    if(actualTime < setTime/2) {
        xp += 0
    } else if (actualTime < setTime) {
        val m = maxXp / (setTime/2)
        xp += m*(actualTime - (setTime/2))
        //y - y1 = m*(x - x1)
        //(x1,y1) = (setTime/2, 0) is a point on the line

    } else if (actualTime < setTime + 5*60) {
        xp += maxXp
    } else if (actualTime < setTime + 30*60) {
        val m = -maxXp/2   //maxXp/2 - maxXp
        xp += m*(actualTime - (setTime + 5*60)) + maxXp
        //y - y1 = m*(x - x1)
        //(x1,y1) = (setTime + 5*60, maxXp) is a point on the line

    } else {
        xp -= maxXp / 2
    }

    xp = max(xp, 0)//limit to at least 0xp
    println(xp)
    return xp
}