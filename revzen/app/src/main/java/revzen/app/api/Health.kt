package revzen.app.api

import com.google.gson.annotations.SerializedName
import revzen.app.R

enum class Health(
    val image: Int
) {
    @SerializedName("0")
    ZERO(R.drawable.heart0),

    @SerializedName("1")
    ONE(R.drawable.heart1),

    @SerializedName("2")
    TWO(R.drawable.heart2),

    @SerializedName("3")
    THREE(R.drawable.heart3)
}