package revzen.app

import revzen.app.api.ApiHandler
import kotlin.math.roundToInt

const val MINS_TO_MILLIS = 60000
const val SECS_TO_MILLIS = 1000
const val SECS_TO_HOURS = 3600

const val STUDY_PET_THRESHOLD = 30

fun timeFormat(time: Int): String {
    val hours = time / 3600
    val mins = (time % 3600) / 60
    val secs = (time % 3600) % 60

    return if(hours == 0){
        if(mins == 0){
            "$secs seconds"
        } else {
            "$mins minutes $secs seconds"
        }
    } else {
        "$hours hours $mins minutes $secs seconds"
    }
}

fun calculateResult(session_data: ArrayList<SessionData>) : ApiHandler.StudyResult {
    var totalStudyTime = 0
    var totalBreakTime = 0
    var totalPlannedStudyTime = 0
    var totalPlannedBreakTime = 0

    var xp = 0
    var health = 0f

    for (session in session_data) {
        totalStudyTime += session.study_time
        totalBreakTime += session.break_time
        totalPlannedStudyTime += session.planned_study_time
        totalPlannedBreakTime += session.planned_break_time

        if (session.study_time > session.planned_study_time) {
            xp += session.study_time
            health += session.study_time / SECS_TO_HOURS
        } else {
            health -= (session.planned_study_time - session.study_time) / SECS_TO_HOURS
        }
    }

    return ApiHandler.StudyResult(
        totalStudyTime,
        totalBreakTime,
        totalPlannedStudyTime,
        totalPlannedBreakTime,
        xp,
        health.roundToInt(),
    )
}