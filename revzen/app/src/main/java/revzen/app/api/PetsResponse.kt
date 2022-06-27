package revzen.app.api
import com.google.gson.annotations.SerializedName

data class PetsResponse(
    @SerializedName("main_pet") val mainPet: Pet,
    @SerializedName("all_pets") val allPets: Map<Pet, PetHealthXp>
)
