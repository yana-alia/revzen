package revzen.app.api

import revzen.app.Pet

class PetInfo(
    val pet: Pet,
    val health: Int,
    val xp: Int,
)

class PetResponse (
    val selectedPet: Pet,
    val allPets: ArrayList<PetInfo>

)