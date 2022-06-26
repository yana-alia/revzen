package revzen.app.api

import revzen.app.R
import com.google.gson.annotations.SerializedName

enum class Health (
    val image: Int)
    {
        @SerializedName("0")
        ZERO(R.drawable.heart0),
        @SerializedName("1")
        ONE(R.drawable.heart1),
        @SerializedName("2")
        TWO(R.drawable.heart2),
        @SerializedName("3")
        THREE(R.drawable.heart3)
    }
