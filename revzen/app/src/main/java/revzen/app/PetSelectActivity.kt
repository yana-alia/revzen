package revzen.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PetSelectActivity : AppCompatActivity() {
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_select)
        findViewById<ImageView>(R.id.shiba).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.husky).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.calico).setBackgroundColor(R.color.transparent_grey)

        //val petData = api request
        val mainPet = Pet.HUSKY //from petData
        when (mainPet) {
            Pet.SHIBA -> findViewById<ImageView>(R.id.shiba).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.HUSKY -> findViewById<ImageView>(R.id.husky).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.CALICO -> findViewById<ImageView>(R.id.calico).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
            Pet.ROCK -> popup()
        }

        //api post request

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
        findViewById<ImageView>(R.id.shiba).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        findViewById<ImageView>(R.id.husky).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.calico).setBackgroundColor(R.color.transparent_grey)
    }

    @SuppressLint("ResourceAsColor")
    fun selectHusky(_view: View) {
        findViewById<ImageView>(R.id.shiba).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.husky).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
        findViewById<ImageView>(R.id.calico).setBackgroundColor(R.color.transparent_grey)
    }

    @SuppressLint("ResourceAsColor")
    fun selectCalico(_view: View) {
        findViewById<ImageView>(R.id.shiba).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.husky).setBackgroundColor(R.color.transparent_grey)
        findViewById<ImageView>(R.id.calico).setBackgroundColor(androidx.appcompat.R.attr.colorPrimary)
    }
}