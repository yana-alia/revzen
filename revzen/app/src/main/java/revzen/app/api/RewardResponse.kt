package revzen.app.api

import com.google.gson.annotations.SerializedName

data class RewardResponse(
    @SerializedName("pet_type")
    val pet: Pet,
    @SerializedName("health")
    val health: Health,
    @SerializedName("xp")
    val XP: Int,
    @SerializedName("pet_change")
    val petChange: PetChange,
)
