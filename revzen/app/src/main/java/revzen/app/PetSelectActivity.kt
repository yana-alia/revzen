package revzen.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.*

class PetSelectActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler

    private lateinit var loading: ProgressBar

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

        loading = findViewById(R.id.petSelectProgressBar)

        shibaImage = findViewById(R.id.shibaImageView)
        huskyImage = findViewById(R.id.huskyImageView)
        calicoImage = findViewById(R.id.calicoImageView)

        shibaHealthImage = findViewById(R.id.shibaHealthImageView)
        huskyHealthImage = findViewById(R.id.huskyHealthImageView)
        calicoHealthImage = findViewById(R.id.calicoHealthImageView)

        shibaImage.setBackgroundColor(R.color.light_grey)
        huskyImage.setBackgroundColor(R.color.light_grey)
        calicoImage.setBackgroundColor(R.color.light_grey)

        shibaXPText = findViewById(R.id.shibaXPTextView)
        huskyXPText = findViewById(R.id.huskyXPTextView)
        calicoXPText = findViewById(R.id.calicoXPTextView)

        loading.visibility = View.VISIBLE

        apiHandler.getPetInfo(this::successInfo, this::failInfo)
    }

    @SuppressLint("SetTextI18n")
    private fun setPetDisplay(
        image: ImageView,
        healthImage: ImageView,
        xpText: TextView,
        pet: Pet,
        info: PetsResponse
    ) {
        if (!info.allPets.containsKey(pet)) {
            image.setImageResource(pet.greyImage)
            image.setOnClickListener {}
            healthImage.setBackgroundColor(androidx.appcompat.R.attr.background)
        } else {
            val petInfo = info.allPets[pet]!!
            image.setImageResource(pet.logoImage)
            xpText.text = "${petInfo.xp} XP"
            healthImage.setImageResource(petInfo.health.image)


            if (info.mainPet == pet) {
                image.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            }
            healthImage.visibility = View.VISIBLE
            xpText.visibility = View.VISIBLE
        }
        image.visibility = View.VISIBLE
    }

    private fun successInfo(info: PetsResponse) {
        loading.visibility = View.INVISIBLE

        if (info.mainPet == Pet.ROCK) {
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

    private fun failInfo(_error: ApiError) {
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

    private fun selectPet(pet: Pet) {
        apiHandler.changePet(pet, {}, this::errorOccurred)
    }

    private fun errorOccurred(_error: ApiError) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage("Could not retrieve pet data from database")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    fun confirmChoice(_view: View) {
        finish()
    }
}