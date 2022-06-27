package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetStatus

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
        var health = info.health.ordinal - 1
        healthImage.setImageResource(info.health.image)

        petImage.visibility = View.VISIBLE
        healthImage.visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        petImage.visibility = View.INVISIBLE
        healthImage.visibility = View.INVISIBLE
    }
}