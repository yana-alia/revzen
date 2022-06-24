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
import revzen.app.api.PetResponse

class PetSelectActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var mainPet: Pet

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_select)
        findViewById<ImageView>(R.id.shibaImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.huskyImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.calicoImage).setBackgroundColor(R.color.light_grey)

        findViewById<ProgressBar>(R.id.pet_loading).visibility = View.VISIBLE
        apiHandler = intent.extras?.getParcelable("handler")!!
        apiHandler.getPetInfo(this::successInfo, this::failInfo)

    }

    private fun successInfo(info: PetResponse) {
        findViewById<ProgressBar>(R.id.pet_loading).visibility = View.INVISIBLE
        mainPet = info.selectedPet

        val shibaHealth = info.allPets[Pet.SHIBA]?.health
        val huskyHealth = info.allPets[Pet.HUSKY]?.health
        val calicoHealth = info.allPets[Pet.CALICO]?.health

        findViewById<ImageView>(R.id.shibaHealth).setImageResource(
            shibaHealth?.image ?: R.drawable.heart0)
        findViewById<TextView>(R.id.shibaXP).text = info.allPets[Pet.SHIBA]?.xp.toString() + " XP"

        findViewById<ImageView>(R.id.huskyHealth).setImageResource(
            huskyHealth?.image ?: R.drawable.heart0)
        findViewById<TextView>(R.id.huskyXP).text = info.allPets[Pet.HUSKY]?.xp.toString() + " XP"

        findViewById<ImageView>(R.id.calicoHealth).setImageResource(
            calicoHealth?.image ?: R.drawable.heart0)
        findViewById<TextView>(R.id.calicoXP).text = info.allPets[Pet.CALICO]?.xp.toString() + " XP"


        when (mainPet) {
            Pet.SHIBA -> findViewById<ImageView>(R.id.shibaImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.HUSKY -> findViewById<ImageView>(R.id.huskyImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.CALICO -> findViewById<ImageView>(R.id.calicoImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.ROCK -> popup()
        }

        findViewById<ImageView>(R.id.shibaImage).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.huskyImage).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.calicoImage).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.shibaHealth).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.huskyHealth).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.calicoHealth).visibility = View.VISIBLE
    }

    private fun failInfo(error: ApiError) {
        findViewById<ProgressBar>(R.id.pet_loading).visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage("Could not retrieve pet data from database")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
        //finish()
    }

    private fun popup() {
        AlertDialog.Builder(this).apply {
            setTitle("OH NO!")
            setMessage("All your pets ran away. But don't worry, Rocky is here to help you out!")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    @SuppressLint("ResourceAsColor")
    fun selectShiba(_view: View) {
        mainPet = Pet.SHIBA
        findViewById<ImageView>(R.id.shibaImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        findViewById<ImageView>(R.id.huskyImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.calicoImage).setBackgroundColor(R.color.light_grey)
    }

    @SuppressLint("ResourceAsColor")
    fun selectHusky(_view: View) {
        mainPet = Pet.HUSKY
        findViewById<ImageView>(R.id.shibaImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.huskyImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        findViewById<ImageView>(R.id.calicoImage).setBackgroundColor(R.color.light_grey)
    }

    @SuppressLint("ResourceAsColor")
    fun selectCalico(_view: View) {
        mainPet = Pet.CALICO
        findViewById<ImageView>(R.id.shibaImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.huskyImage).setBackgroundColor(R.color.light_grey)
        findViewById<ImageView>(R.id.calicoImage).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
    }

    fun confirmChoice(_view: View){
        apiHandler.changePet(mainPet,{finish()},{})
        println(mainPet)
    }
}