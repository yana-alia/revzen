package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import revzen.app.api.*
import kotlin.math.max

class FailActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler

    private lateinit var petImage: ImageView
    private lateinit var healthImage: ImageView

    private lateinit var timeTracker: SessionData
    private lateinit var studyList : ArrayList<SessionData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        petImage = findViewById(R.id.imageView)
        healthImage = findViewById(R.id.imageView2)

        apiHandler.logSession(timeTracker, {}, {})
        apiHandler.getCurrentPet(this::successGet, this::failGet)
    }

    override fun onBackPressed() {}

    fun goToSummary(_view: View) {
        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun successGet(info: PetStatus) {
        val mainPet = info.petType
        petImage.setImageResource(mainPet.failImage)
        val healthNum = max(info.health.ordinal - 1,0)
        val newHealth = Health.values()[healthNum]
        healthImage.setImageResource(newHealth.image)

        petImage.visibility = View.VISIBLE
        healthImage.visibility = View.VISIBLE

        if(newHealth == Health.ZERO && mainPet != Pet.ROCK){
            AlertDialog.Builder(this).apply {
                setTitle("OH NO!")
                setMessage("Your current pet has ran out of health. You can no longer study with this pet.")
                setPositiveButton("Ok") { _, _ -> finish() }
                create()
                show()
            }
        }
    }

    private fun failGet(error: ApiError) {
        petImage.visibility = View.INVISIBLE
        healthImage.visibility = View.INVISIBLE
    }
}