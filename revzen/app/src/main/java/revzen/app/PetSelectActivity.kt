package revzen.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetInfo
import revzen.app.api.PetResponse

class PetSelectActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler

    private lateinit var loading : ProgressBar
    
    private lateinit var shibaImage: ImageView
    private lateinit var huskyImage: ImageView
    private lateinit var calicoImage: ImageView

    private lateinit var shibaHealthImage: ImageView
    private lateinit var huskyHealthImage: ImageView
    private lateinit var calicoHealthImage: ImageView

    private lateinit var shibaXPText: TextView
    private lateinit var huskyXPText: TextView
    private lateinit var calicoXPText: TextView

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_select)

        apiHandler = intent.extras?.getParcelable("handler")!!
        
        loading = findViewById(R.id.pet_loading)
        
        shibaImage = findViewById(R.id.shibaImage)
        huskyImage = findViewById(R.id.huskyImage)
        calicoImage = findViewById(R.id.calicoImage)

        shibaHealthImage = findViewById(R.id.shibaHealth)
        huskyHealthImage = findViewById(R.id.huskyHealth)
        calicoHealthImage = findViewById(R.id.calicoHealth)

        shibaImage.setBackgroundColor(R.color.light_grey)
        huskyImage.setBackgroundColor(R.color.light_grey)
        calicoImage.setBackgroundColor(R.color.light_grey)

        shibaXPText = findViewById(R.id.shibaXP)
        huskyXPText = findViewById(R.id.huskyXP)
        calicoXPText = findViewById(R.id.calicoXP)
        
        loading.visibility = View.VISIBLE

        apiHandler.getPetInfo(this::successInfo, this::failInfo)
    }

    @SuppressLint("SetTextI18n")
    private fun setPetDisplay(image: ImageView, healthImage: ImageView, xpText: TextView, pet: Pet, info: PetResponse) {
        val petInfo =  info.allPets[pet]!!

        if (petInfo.health == Health.ZERO) {
            image.setImageResource(pet.greyImage)
            image.setOnClickListener {}
            healthImage.setBackgroundColor(androidx.appcompat.R.attr.background)
        } else {
            image.setImageResource(pet.logoImage)
        }
        xpText.text = "${petInfo.xp} XP"
        healthImage.setImageResource(petInfo.health.image)

        if (info.selectedPet == pet) {
            image.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        }
        image.visibility = View.VISIBLE
        xpText.visibility = View.VISIBLE
    }

    private fun successInfo(info: PetResponse) {
        loading.visibility = View.INVISIBLE

        if (info.selectedPet == Pet.ROCK) {
            AlertDialog.Builder(this).apply {
                setTitle("OH NO!")
                setMessage("All your pets ran away. But don't worry, Rocky is here to help you out!")
                setPositiveButton("Ok") { _, _ -> finish() }
                create()
                show()
            }
        } else {
            setPetDisplay(shibaImage, shibaHealthImage, shibaXPText, Pet.SHIBA, info)
            setPetDisplay(huskyImage, huskyHealthImage, huskyXPText, Pet.HUSKY, info)
            setPetDisplay(calicoImage, calicoHealthImage, calicoXPText, Pet.CALICO, info)
        }
    }

    private fun failInfo(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage("Could not retrieve pet data from database")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    @SuppressLint("ResourceAsColor")
    fun selectShiba(_view: View) {
        shibaImage.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        huskyImage.setBackgroundColor(R.color.light_grey)
        calicoImage.setBackgroundColor(R.color.light_grey)
        selectPet(Pet.SHIBA)
    }

    @SuppressLint("ResourceAsColor")
    fun selectHusky(_view: View) {
        shibaImage.setBackgroundColor(R.color.light_grey)
        huskyImage.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        calicoImage.setBackgroundColor(R.color.light_grey)
        selectPet(Pet.HUSKY)
    }

    @SuppressLint("ResourceAsColor")
    fun selectCalico(_view: View) {
        shibaImage.setBackgroundColor(R.color.light_grey)
        huskyImage.setBackgroundColor(R.color.light_grey)
        calicoImage.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        selectPet(Pet.CALICO)
    }

    fun selectPet(pet: Pet) {
        apiHandler.changePet(pet, {}, this::errorOccured)
    }

    fun errorOccured(error: ApiError) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage("Could not retrieve pet data from database")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    fun confirmChoice(_view: View){
        finish()
    }
}