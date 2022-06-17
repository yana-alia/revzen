package revzen.app.api

import com.google.gson.annotations.SerializedName

data class HistoryResponse (
    @SerializedName("time") val time : TimeResponse,
    @SerializedName("planned_study_time") val planned_study_time : Int,
    @SerializedName("planned_break_time") val planned_break_time : Int,
    @SerializedName("study_time") val study_time : Int,
    @SerializedName("break_time") val break_time : Int
)