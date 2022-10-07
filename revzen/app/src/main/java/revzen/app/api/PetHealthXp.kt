package revzen.app.api

import com.google.gson.annotations.SerializedName

data class PetHealthXp(
    @SerializedName("health") val health: Health,
    @SerializedName("xp") val xp: Int
)
