package revzen.app.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessionData(
    var study_time: Int,
    var break_time: Int,
    var planned_study_time: Int,
    var planned_break_time: Int
) :
    Parcelable