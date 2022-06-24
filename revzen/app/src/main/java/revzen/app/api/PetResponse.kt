package revzen.app.api

import revzen.app.Health
import revzen.app.Pet

class PetInfo(
    val health: Health,
    val xp: Int,
)

class PetResponse (
    val selectedPet: Pet,
    val allPets: HashMap<Pet, PetInfo>

)