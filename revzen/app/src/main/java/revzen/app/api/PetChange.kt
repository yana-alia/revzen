package revzen.app.api

import com.google.gson.annotations.SerializedName

enum class PetChange {
    @SerializedName("0")
    NoChange,

    @SerializedName("1")
    SwitchedPet,

    @SerializedName("2")
    OnlyRock
}
