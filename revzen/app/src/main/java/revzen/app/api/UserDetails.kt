package revzen.app.api

import com.google.gson.annotations.SerializedName

data class UserDetails(
    @SerializedName("friendcode") val friendcode : Int,
    @SerializedName("username") val username : String
)
