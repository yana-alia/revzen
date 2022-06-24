package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetResponse

class FailActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)
        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        apiHandler.stopLiveRevision({}, { })
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        apiHandler.getPetInfo(this::successGet, this::failGet)

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
            { })
        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun successGet(info: PetResponse) {
        val mainPet = info.selectedPet
        findViewById<ImageView>(R.id.imageView).setImageResource(mainPet.failImage)
        findViewById<ImageView>(R.id.imageView).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.imageView2).setImageResource(
            info.allPets[mainPet]?.health?.image
                ?: R.drawable.heart3)
        findViewById<ImageView>(R.id.imageView2).visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        findViewById<ImageView>(R.id.imageView).visibility = View.INVISIBLE
        findViewById<ImageView>(R.id.imageView2).visibility = View.INVISIBLE
    }
}