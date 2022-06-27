package revzen.app.api

import com.google.gson.annotations.SerializedName

data class GiveResult(
    @SerializedName("pet") val pet: Pet,
    @SerializedName("is_new") val isNew: Boolean
)
