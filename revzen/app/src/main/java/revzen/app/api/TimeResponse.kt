package revzen.app.api

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp
import java.util.*

data class TimeResponse(
    @SerializedName("secs_since_epoch") val secs_since_epoch: Int,
    @SerializedName("nanos_since_epoch") val nanos_since_epoch: Int
) {
    val time: Date
        get() {
            return Date(Timestamp(secs_since_epoch.toLong()).time)
        }
}
