package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetsResponse

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private lateinit var apiHandler: ApiHandler

    private lateinit var timeTracker: SessionData
    private var studyList = ArrayList<SessionData>()

    private var inSession = true
    private var validLeave = false
    private var originalTime = 0L
    
    private lateinit var studyTitle: TextView
    private lateinit var warning: TextView
    private lateinit var endButton: Button
    private lateinit var petImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!

        // updating getting the api handler
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        studyTitle = findViewById(R.id.studyTitleText)
        warning = findViewById(R.id.warningView)
        endButton = findViewById(R.id.endSessionButton)
        petImage = findViewById(R.id.petView)

        //api request to get main pet
        apiHandler.getPetInfo(this::successGet, this::failGet)

        timer = findViewById(R.id.chronometer)
        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (timeTracker.planned_study_time * SECS_TO_MILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime(): Int =
        ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.study_time += getElapsedTime()
    }

    override fun onUserLeaveHint() {
        if (!validLeave) {
            super.onUserLeaveHint()
            timer.stop()
            updateTimeTracker()
            goToFail()
        }
    }

    //disable back button by preventing call to super.onBackPressed()
    override fun onBackPressed() {}

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L) {
            chronometer.base -= (1000)
        }
        if ((elapsedMillis <= 0) && inSession) {
            setBreakView()
            inSession = false
        } else if ((elapsedMillis > 0) && !inSession) {
            setTimerView()
            inSession = true
        } else if (elapsedMillis < STUDY_FAIL_THRESHOLD) {
            timer.stop()
            goToFail()
        } else if (elapsedMillis < STUDY_WARNING_THRESHOLD) {
            setWarningView()
        }
    }

    private fun setBreakView() {
        studyTitle.text = resources.getString(R.string.break_title)
        warning.visibility = View.INVISIBLE
        endButton.text =
            resources.getString(R.string.break_button)
    }

    private fun setTimerView() {
        studyTitle.text = resources.getString(R.string.session_title)
        warning.visibility = View.VISIBLE
        endButton.text = resources.getString(R.string.end_session_button)
    }

    private fun setWarningView() {
        warning.text = resources.getString(R.string.warning_title2)
        warning.visibility = View.VISIBLE
    }

    private fun goToFail(){
        validLeave = true
        startActivity(Intent(this, FailActivity::class.java).apply {
            putExtra("reason", "studyTimeout")
            putExtra("handler", apiHandler)
            putExtra("timeTracker", timeTracker)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })

        finish()
    }

    fun goToEndSession(_view: View) {
        validLeave = true
        timer.stop()
        updateTimeTracker()

        startActivity(if (inSession) {
            Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            }
        } else {
            Intent(this, BreakActivity::class.java).apply {
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList)
            }
        })
        finish()
    }

    private fun successGet(info: PetsResponse) {
        petImage.setImageResource(info.mainPet.studyImage)
        petImage.visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        petImage.visibility = View.INVISIBLE
    }
}