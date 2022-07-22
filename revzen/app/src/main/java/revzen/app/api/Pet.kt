package revzen.app.api

import com.google.gson.annotations.SerializedName
import revzen.app.R

enum class Pet(
    val logoImage: Int,
    val studyImage: Int,
    val breakImage: Int,
    val failImage: Int,
    val greyImage: Int,
    val petName: String
) {
    @SerializedName("0")
    ROCK(
        R.drawable.petlogo_rock,
        R.drawable.petstudy_rock,
        R.drawable.petbreak_rock,
        R.drawable.petfail_rock,
        R.drawable.petgrey_rock,
        "Rock"
    ),

    @SerializedName("1")
    SHIBA(
        R.drawable.petlogo_shiba,
        R.drawable.petstudy_shiba,
        R.drawable.petbreak_shiba,
        R.drawable.petfail_shiba,
        R.drawable.petgrey_shiba,
        "Shiba"
    ),

    @SerializedName("2")
    HUSKY(
        R.drawable.petlogo_husky,
        R.drawable.petstudy_husky,
        R.drawable.petbreak_husky,
        R.drawable.petfail_husky,
        R.drawable.petgrey_husky,
        "Husky"
    ),

    @SerializedName("3")
    CALICO(
        R.drawable.petlogo_calico,
        R.drawable.petstudy_calico,
        R.drawable.petbreak_calico,
        R.drawable.petfail_calico,
        R.drawable.petgrey_calico,
        "Calico"
    )
}
