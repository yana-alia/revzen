package revzen.app

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetStatus

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private lateinit var breakWarning: TextView
    private lateinit var petImage: ImageView

    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var originalTime = 0L
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        apiHandler.getCurrentPet(this::successGet, this::failGet)

        startService(Intent(this, BgBreakService::class.java).apply {
            putExtra("breakLength", timeTracker.planned_break_time)
        })

        timer = findViewById(R.id.breakTimer)
        breakWarning = findViewById(R.id.breakWarning)
        petImage = findViewById(R.id.petView)

        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (timeTracker.planned_break_time * SECS_TO_MILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime(): Int =
        ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.break_time += getElapsedTime()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun keepStudying(_view: View) {
        timer.stop()
        updateTimeTracker()
        startActivity(Intent(this, SetupActivity::class.java).apply {
            putExtra("handler", apiHandler)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })
        finish()
    }

    fun endSession(_view: View) {
        apiHandler.stopLiveRevision({}, { })

        timer.stop()
        updateTimeTracker()

        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })
        finish()
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L) {
            chronometer.base -= (1000)
        } else if (elapsedMillis < -5 * MINS_TO_MILLIS) {
            startActivity(Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            })
            finish()
        } else if (elapsedMillis < 0) {
            breakWarning.visibility = View.VISIBLE
        }
    }

    private fun successGet(info: PetStatus) {
        petImage.setImageResource(info.petType.breakImage)
        petImage.visibility = View.VISIBLE
    }

    //todo improve
    private fun failGet(error: ApiError) {
        apiHandler.stopLiveRevision({}, { })
        finish()
    }
}