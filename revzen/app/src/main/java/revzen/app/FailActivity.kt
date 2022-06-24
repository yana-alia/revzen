package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import revzen.app.api.ApiHandler

class FailActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)
        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        apiHandler.stopLiveRevision({}, { _ -> })
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        //api request to get main pet
        val mainPet = Pet.HUSKY
        when (mainPet) {
            Pet.SHIBA -> findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.petfail_shiba)
            Pet.HUSKY -> findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.petfail_husky)
            Pet.CALICO -> findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.petfail_calico)
            Pet.ROCK -> findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.petfail_rock)
        }

        //API call to set health variable
        val health = 2
        val healthBar = findViewById<ImageView>(R.id.imageView2)
        val image = when (health){
            3 -> R.drawable.heart3
            2 -> R.drawable.heart2
            1 -> R.drawable.heart1
            0 -> R.drawable.heart0
            else -> R.drawable.heart3
        }
        healthBar.setImageResource(image)
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToSummary(_view: View) {
        apiHandler.logSession(
            timeTracker.planned_study_time,
            timeTracker.planned_break_time,
            timeTracker.study_time,
            timeTracker.break_time,
            {},
            { _ -> })
        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }
}