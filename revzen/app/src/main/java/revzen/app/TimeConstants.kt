package revzen.app

import revzen.app.api.ApiHandler
import revzen.app.api.SessionData
import kotlin.math.max

const val MINS_TO_MILLIS = 60000
const val SECS_TO_MILLIS = 1000

const val STUDY_WARNING_THRESHOLD = -20 * MINS_TO_MILLIS
const val STUDY_FAIL_THRESHOLD = -30 * MINS_TO_MILLIS
const val BREAK_FAIL_THRESHOLD = -5 * MINS_TO_MILLIS

const val GIVE_PET_CHANCE = 2

fun timeFormat(time: Int): String {
    val hours = time / 3600
    val mins = (time % 3600) / 60
    val secs = (time % 3600) % 60
    var out = ""

    if (hours != 0) {
        out += "$hours hour"
        out += if (hours == 1) {
            " "
        } else {
            "s "
        }
    }
    if (mins != 0) {
        out += "$mins minute"
        out += if (mins == 1) {
            " "
        } else {
            "s "
        }
    }
    if (secs != 0) {
        out += "$secs second"
        out += if (secs == 1) {
            " "
        } else {
            "s "
        }
    }

    return out
}


fun calculateResult(session_data: ArrayList<SessionData>): ApiHandler.Reward {
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

        if (session.study_time >= session.planned_study_time) {
            xpGained += getXp(session.study_time, session.planned_study_time)
        } else {
            healthChange = -1
        }
    }

    return ApiHandler.Reward(
        xpGained,
        healthChange,
        totalStudyTime,
        totalBreakTime
    )
}

fun getXp(actualTime: Int, setTime: Int): Int {
    // Values are in Seconds
    var xp = 0
    val maxXp = setTime / 10

    if (actualTime < setTime / 2) {
        xp += 0
    } else if (actualTime < setTime) {
        val m = maxXp / (setTime / 2)
        xp += m * (actualTime - (setTime / 2))
        //y - y1 = m*(x - x1)
        //(x1,y1) = (setTime/2, 0) is a point on the line

    } else if (actualTime < setTime + 5 * 60) {
        xp += maxXp
    } else if (actualTime < setTime + 30 * 60) {
        val m = -maxXp / 2   //maxXp/2 - maxXp
        xp += m * (actualTime - (setTime + 5 * 60)) + maxXp
        //y - y1 = m*(x - x1)
        //(x1,y1) = (setTime + 5*60, maxXp) is a point on the line

    } else {
        xp -= maxXp / 2
    }

    xp = max(xp, 0)//limit to at least 0xp
    return xp
}