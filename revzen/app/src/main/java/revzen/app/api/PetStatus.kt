package revzen.app.api

import com.google.gson.annotations.SerializedName

data class PetStatus(
    @SerializedName("pet_type") val petType: Pet,
    @SerializedName("health") val health: Health,
    @SerializedName("xp") val xp: Int
)